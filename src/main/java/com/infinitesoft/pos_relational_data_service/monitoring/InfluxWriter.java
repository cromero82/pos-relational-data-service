package com.infinitesoft.pos_relational_data_service.monitoring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * Async writer for InfluxDB 3 line protocol.
 *
 * <p>Pipeline:
 * <pre>
 *   producer threads -> in-memory queue -> worker thread -> HTTP POST /api/v3/write_lp
 *                                                       \-> on failure: spool to disk
 *   retry-thread (cada N s) -> reads spool files -> POST -> on success: deletes file
 * </pre>
 *
 * <p>Design rules:
 * <ul>
 *   <li><b>Never throw</b> on the producer side. The business app must never
 *       crash because the monitoring service is down or slow.</li>
 *   <li><b>Bounded memory</b>. If the queue is full we spool the new event
 *       directly to disk instead of blocking.</li>
 *   <li><b>Survive restarts</b>. Pending lines persist in the spool dir; the
 *       retry job picks them up on next startup.</li>
 * </ul>
 */
@Service
public class InfluxWriter {

    private static final Logger log = LogManager.getLogger(InfluxWriter.class);

    private final InfluxProperties props;
    private final WebClient web;

    private BlockingQueue<String> queue;
    private ExecutorService       worker;
    private ScheduledExecutorService retryExec;
    private Path                  spoolDir;
    private final AtomicBoolean   running = new AtomicBoolean(false);
    private final AtomicLong      droppedCounter = new AtomicLong();
    private final AtomicLong      spooledCounter = new AtomicLong();
    private final AtomicLong      sentCounter    = new AtomicLong();

    public InfluxWriter(InfluxProperties props,
                        @Qualifier("influxWebClient") WebClient web) {
        this.props = props;
        this.web   = web;
    }

    @PostConstruct
    public void start() throws IOException {
        spoolDir = Paths.get(props.getSpoolDir()).toAbsolutePath();
        Files.createDirectories(spoolDir);

        queue = new LinkedBlockingQueue<>(props.getQueueCapacity());

        worker = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "influx-writer");
            t.setDaemon(true);
            return t;
        });
        retryExec = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "influx-spool-retry");
            t.setDaemon(true);
            return t;
        });

        running.set(true);
        worker.submit(this::workerLoop);
        retryExec.scheduleWithFixedDelay(this::retrySpooledSafely,
                props.getRetryIntervalMs(), props.getRetryIntervalMs(), TimeUnit.MILLISECONDS);

        InfluxStaticHolder.set(this);

        log.info("InfluxWriter started: url={} db={} spool={} batch={} flush={}ms",
                props.getUrl(), props.getDatabase(), spoolDir,
                props.getBatchSize(), props.getFlushIntervalMs());
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        if (worker != null) worker.shutdownNow();
        if (retryExec != null) retryExec.shutdownNow();

        // Drain whatever is still in the queue to disk so we don't lose it
        if (queue != null && !queue.isEmpty()) {
            List<String> remaining = new ArrayList<>(queue.size());
            queue.drainTo(remaining);
            if (!remaining.isEmpty()) {
                spoolBatch(remaining);
                log.info("InfluxWriter shutdown: spooled {} pending lines to disk", remaining.size());
            }
        }
        log.info("InfluxWriter stats: sent={} spooled={} dropped={}",
                sentCounter.get(), spooledCounter.get(), droppedCounter.get());
    }

    // ===== Producer API ====================================================

    /**
     * Non-blocking enqueue. If the queue is full, the line is written
     * directly to the spool dir so it isn't lost.
     */
    public void enqueue(LineProtocol.Line line) {
        if (line == null) return;
        String s = line.toString();
        boolean accepted = queue.offer(s);
        if (!accepted) {
            // Queue full -> persist single line to disk to avoid blocking caller
            spoolBatch(java.util.Collections.singletonList(s));
        }
    }

    public long getSentCount()    { return sentCounter.get();    }
    public long getSpooledCount() { return spooledCounter.get(); }
    public long getDroppedCount() { return droppedCounter.get(); }

    // ===== Worker loop =====================================================

    private void workerLoop() {
        List<String> batch = new ArrayList<>(props.getBatchSize());
        while (running.get()) {
            try {
                // Wait up to flushInterval for the first line of a new batch
                String first = queue.poll(props.getFlushIntervalMs(), TimeUnit.MILLISECONDS);
                if (first != null) batch.add(first);

                // Drain whatever else is immediately available
                queue.drainTo(batch, props.getBatchSize() - batch.size());

                if (!batch.isEmpty()) {
                    boolean ok = postBatch(batch);
                    if (!ok) {
                        spoolBatch(batch);
                    } else {
                        sentCounter.addAndGet(batch.size());
                    }
                    batch.clear();
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable t) {
                // Worker MUST never die. Log and continue.
                log.warn("InfluxWriter worker iteration failed", t);
                if (!batch.isEmpty()) {
                    spoolBatch(batch);
                    batch.clear();
                }
            }
        }
    }

    // ===== HTTP POST =======================================================

    private static final Duration POST_TIMEOUT = Duration.ofSeconds(10);

    /** Returns true if the batch was accepted (HTTP 2xx). */
    private boolean postBatch(List<String> lines) {
        if (lines.isEmpty()) return true;
        String body = String.join("\n", lines);
        try {
            web.post()
                .uri(uri -> uri.path("/api/v3/write_lp")
                        .queryParam("db", props.getDatabase())
                        .queryParam("precision", "ns")
                        .queryParam("accept_partial", "true")
                        .build())
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block(POST_TIMEOUT);
            return true;
        } catch (Throwable t) {
            // Don't spam logs on every single failure — only when it changes severity
            log.debug("InfluxWriter POST failed ({} lines): {}", lines.size(), t.getMessage());
            return false;
        }
    }

    // ===== Spool to disk ===================================================

    private void spoolBatch(List<String> lines) {
        try {
            String fname = String.format("%d-%04d.lp",
                    System.currentTimeMillis(),
                    (int)(Math.random() * 10000));
            Path file = spoolDir.resolve(fname);
            // Write atomically (.tmp -> rename) so the retry thread never sees half files
            Path tmp = spoolDir.resolve(fname + ".tmp");
            Files.write(tmp, String.join("\n", lines).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE);
            spooledCounter.addAndGet(lines.size());
        } catch (IOException io) {
            // Last resort: drop. Better than blocking the producer.
            droppedCounter.addAndGet(lines.size());
            log.warn("InfluxWriter could not spool batch ({} lines), dropping: {}",
                    lines.size(), io.getMessage());
        }
    }

    // ===== Retry job =======================================================

    private void retrySpooledSafely() {
        try {
            retrySpooled();
        } catch (Throwable t) {
            log.warn("InfluxWriter retry iteration failed", t);
        }
    }

    /** Walks the spool dir, sends each file's content; on success, deletes the file. */
    private void retrySpooled() throws IOException {
        if (!Files.isDirectory(spoolDir)) return;
        // Quick health probe before reading files (avoid spinning POSTs against a dead server)
        if (!healthyQuick()) return;

        try (Stream<Path> files = Files.list(spoolDir)) {
            files
                .filter(p -> p.getFileName().toString().endsWith(".lp"))
                .sorted()                          // oldest first (timestamp prefix)
                .limit(50)                         // per-cycle cap
                .forEach(this::sendSpoolFile);
        }
    }

    private void sendSpoolFile(Path file) {
        try {
            byte[] data = Files.readAllBytes(file);
            if (data.length == 0) {
                Files.deleteIfExists(file);
                return;
            }
            String body = new String(data, StandardCharsets.UTF_8);
            int lineCount = (int) body.chars().filter(c -> c == '\n').count() + 1;

            web.post()
                .uri(uri -> uri.path("/api/v3/write_lp")
                        .queryParam("db", props.getDatabase())
                        .queryParam("precision", "ns")
                        .queryParam("accept_partial", "true")
                        .build())
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block(POST_TIMEOUT);

            Files.deleteIfExists(file);
            sentCounter.addAndGet(lineCount);
            spooledCounter.addAndGet(-lineCount);   // not strictly accurate but useful
        } catch (Throwable t) {
            // leave the file in place; retried next cycle
            log.debug("Spool file retry failed for {}: {}", file.getFileName(), t.getMessage());
        }
    }

    /** Cheap GET /health to short-circuit retries when Influx is still down. */
    private boolean healthyQuick() {
        try {
            return Boolean.TRUE.equals(
                web.get()
                   .uri("/health")
                   .retrieve()
                   .toBodilessEntity()
                   .map(r -> r.getStatusCode().is2xxSuccessful())
                   .onErrorReturn(false)
                   .block(Duration.ofSeconds(2))
            );
        } catch (Throwable t) {
            return false;
        }
    }
}
