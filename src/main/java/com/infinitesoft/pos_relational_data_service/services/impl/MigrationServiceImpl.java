package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.HistorialProducto;
import com.infinitesoft.pos_relational_data_service.entities.Product;
import com.infinitesoft.pos_relational_data_service.repositories.ProductRepository;
import com.infinitesoft.pos_relational_data_service.services.HistorialProductoService;
import com.infinitesoft.pos_relational_data_service.services.MigrationResult;
import com.infinitesoft.pos_relational_data_service.services.MigrationService;
import com.infinitesoft.pos_relational_data_service.services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class MigrationServiceImpl implements MigrationService {

    private static final Logger log = LoggerFactory.getLogger(MigrationServiceImpl.class);

    private static final String BARCODE_REGEX = "^\\d{5,30}$"; // Numeric barcode: 5–30 digits (broad support for various local/legacy formats)

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private HistorialProductoService historialProductoService;

    @Override
    public MigrationResult importVentasYa(MultipartFile file, String eventName) {
        if (file == null || file.isEmpty()) {
            return MigrationResult.error("Empty file");
        }
        if (eventName == null || eventName.trim().isEmpty()) {
            return MigrationResult.error("Missing eventName");
        }

        int created = 0;
        int skipped = 0;
        int duplicates = 0;
        int errors = 0;
        List<String> messages = new ArrayList<>();

        String filename = file.getOriginalFilename();
        log.info("[IMPORT] Starting importVentasYa | event='{}' | file='{}'", eventName, filename);

        try {
            // Read all bytes to allow charset detection
            byte[] bytes = file.getBytes();
            Charset detected = detectCharset(bytes);
            String content = new String(bytes, detected);
            if (log.isInfoEnabled()) {
                log.info("[IMPORT] Using charset '{}' for file='{}'", detected.name(), filename);
            }
            // Remove potential UTF-8 BOM from content start
            if (!content.isEmpty() && content.charAt(0) == '\uFEFF') {
                content = content.substring(1);
            }
            try (BufferedReader br = new BufferedReader(new StringReader(content))) {
                String line;
                int lineNo = 0;
                while ((line = br.readLine()) != null) {
                    lineNo++;
                    String raw = line.trim();
                    if (raw.isEmpty()) {
                        log.debug("[IMPORT] Line {}: empty/blank, skipped", lineNo);
                        continue;
                    }

                if (looksLikeHeader(raw)) {
                    // Skip header row
                    log.debug("[IMPORT] Line {}: header detected, skipped: {}", lineNo, raw);
                    continue;
                }

                log.trace("[IMPORT] Line {} RAW: {}", lineNo, raw);

                String[] cols = splitFlexible(raw);
                if (cols.length < 2) {
                    skipped++;
                    // Only log, do not add to messages (messages should contain only failed imports)
                    log.warn("[IMPORT] Line {}: not enough columns ({}), skipped | cols={} | raw='{}'", lineNo, cols.length, Arrays.toString(cols), raw);
                    continue;
                }

                String first = safeGet(cols, 0);
                boolean firstIsBarcode = first != null && first.matches(BARCODE_REGEX);
                String second = safeGet(cols, 1);
                boolean secondIsBarcode = second != null && second.matches(BARCODE_REGEX);

                String codigo;
                String descripcion;
                String precioCostoStr;
                String precioVentaStr;
                String pendingMsg = null; // message to append later with full context

                // Explicit handling for 8+ column lines where code/description might be inverted or both are descriptions
                if (cols.length >= 8 && first != null && second != null && (firstIsBarcode ^ secondIsBarcode)) {
                    if (firstIsBarcode) {
                        // Regular order: [0]=barcode, [1]=description
                        codigo = first;
                        descripcion = second;
                        precioCostoStr = safeGet(cols, 2);
                        precioVentaStr = safeGet(cols, 3);
                        log.debug("[IMPORT] Line {}: scenario=BARCODE+DESC (8+) | codigo='{}' | descripcion='{}'", lineNo, codigo, descripcion);
                    } else {
                        // Inverted order: [0]=description, [1]=barcode
                        codigo = second;
                        descripcion = first;
                        precioCostoStr = safeGet(cols, 2);
                        precioVentaStr = safeGet(cols, 3);
                        log.debug("[IMPORT] Line {}: scenario=INVERTED (8+) | codigo='{}' | descripcion='{}'", lineNo, codigo, descripcion);
                    }
                } else if (first != null && second != null && !firstIsBarcode && !secondIsBarcode && !looksLikeMoney(second) && cols.length >= 4) {
                    // New scenario: both first and second look like descriptions (no valid barcode). Use first as 'codigo' and second as 'descripcion'. Applies for 4+ columns.
                    codigo = first;
                    descripcion = second;
                    precioCostoStr = safeGet(cols, 2);
                    precioVentaStr = safeGet(cols, 3);
                    pendingMsg = "both first tokens are not barcode – using first as codigo and second as descripcion";
                    log.debug("[IMPORT] Line {}: {} | codigo='{}' | descripcion='{}'", lineNo, pendingMsg, codigo, descripcion);
                } else if (!firstIsBarcode) {
                    // Scenario: first column is description, no barcode provided (or ambiguous)
                    codigo = "";
                    descripcion = first;
                    precioCostoStr = safeGet(cols, 1);
                    precioVentaStr = safeGet(cols, 2);
                    log.debug("[IMPORT] Line {}: scenario=NO_BARCODE descriptionOnly | descripcion='{}'", lineNo, descripcion);
                } else if (cols.length == 7) {
                    // Scenario: barcode present but description missing (7 columns total)
                    codigo = first;
                    descripcion = "";
                    precioCostoStr = safeGet(cols, 1);
                    precioVentaStr = safeGet(cols, 2);
                    pendingMsg = "barcode '" + codigo + "' without description – registered with warning";
                    log.warn("[IMPORT] Line {}: {}", lineNo, pendingMsg);
                } else {
                    // Default: barcode and description present in first two columns
                    codigo = first;
                    descripcion = safeGet(cols, 1);
                    precioCostoStr = safeGet(cols, 2);
                    precioVentaStr = safeGet(cols, 3);
                    log.debug("[IMPORT] Line {}: scenario=BARCODE+DESC | codigo='{}' | descripcion='{}'", lineNo, codigo, descripcion);
                }

                // Fallback to heuristics if parsed tokens don't look like money
                if (!looksLikeMoney(precioCostoStr)) {
                    String before = precioCostoStr;
                    precioCostoStr = findPrecioCosto(cols);
                    log.debug("[IMPORT] Line {}: resolved precioCosto from '{}' -> '{}' via heuristic", lineNo, before, precioCostoStr);
                }
                if (!looksLikeMoney(precioVentaStr)) {
                    String before = precioVentaStr;
                    precioVentaStr = findPrecioVenta(cols);
                    log.debug("[IMPORT] Line {}: resolved precioVenta from '{}' -> '{}' via heuristic", lineNo, before, precioVentaStr);
                }

                // After resolving prices: do not include non-failure info/warnings in messages; just log context for diagnostics
                if (pendingMsg != null) {
                    String ctxSummary = "codigo='" + codigo + "', descripcion='" + descripcion + "', precioCosto='" + precioCostoStr + "', precioVenta='" + precioVentaStr + "', cols=" + Arrays.toString(cols);
                    log.debug("[IMPORT] Line {}: {} | {}", lineNo, pendingMsg, ctxSummary);
                }

                Double precioVenta = parseMoneyToDouble(precioVentaStr).orElse(0.0);
                Double precioCosto = parseMoneyToDouble(precioCostoStr).orElse(0.0);
                log.debug("[IMPORT] Line {}: parsed precioCosto={} precioVenta={}", lineNo, precioCosto, precioVenta);

                // Check duplicates only if a non-empty barcode is provided
                if (codigo != null && !codigo.trim().isEmpty()) {
                    String normalizedBarcode = codigo.toUpperCase(Locale.ROOT);
                    if (productRepository.findByBarcode(normalizedBarcode).isPresent()) {
                        duplicates++;
                        String ctxSummary = "codigo='" + codigo + "', descripcion='" + descripcion + "', precioCosto='" + precioCostoStr + "', precioVenta='" + precioVentaStr + "', cols=" + Arrays.toString(cols);
                        String dupMsg = "Line " + lineNo + ": duplicate barcode '" + codigo + "' – skipped | " + ctxSummary;
                        // Do not add to messages; duplicates are not failures
                        log.warn("[IMPORT] {}", dupMsg);
                        continue;
                    }
                }

                try {
                    // Persist with null barcode if empty to avoid unique constraint collisions
                    String barcodeToSave = (codigo == null || codigo.trim().isEmpty()) ? null : codigo;
                    Product p = Product.builder()
                            .barcode(barcodeToSave)
                            .nombre((descripcion == null || descripcion.trim().isEmpty()) ? (barcodeToSave == null ? "" : barcodeToSave) : descripcion)
                            .precio(precioVenta)
                            .precioCompra(precioCosto)
                            .build();

                    Product saved = productService.create(p);
                    log.info("[IMPORT] Line {}: CREATED product id={} | barcode='{}' | nombre='{}' | precioVenta={} | precioCosto={}", lineNo, saved.getId(), saved.getBarcode(), saved.getNombre(), precioVenta, precioCosto);

                    BigDecimal historialPrecio = toSafeMoney(saved.getPrecio());

                    HistorialProducto hp = HistorialProducto.builder()
                            .productoId(saved.getId())
                            .evento(eventName)
                            .precio(historialPrecio)
                            .activo(true)
                            .build();
                    historialProductoService.create(hp);
                    log.debug("[IMPORT] Line {}: HISTORIAL recorded for productoId={} event='{}' precio={}", lineNo, saved.getId(), eventName, historialPrecio);

                    created++;
                } catch (Exception ex) {
                    errors++;
                    StringBuilder ctx = new StringBuilder();
                    ctx.append("codigo='").append(codigo).append("', ")
                       .append("descripcion='").append(descripcion).append("', ")
                       .append("precioCosto='").append(precioCostoStr).append("', ")
                       .append("precioVenta='").append(precioVentaStr).append("', ")
                       .append("cols=").append(Arrays.toString(cols));
                    String err = ex.getClass().getSimpleName() + ": " + (ex.getMessage() == null ? "(no message)" : ex.getMessage());
                    messages.add("Line " + lineNo + ": error - " + err + " | " + ctx);
                    log.error("[IMPORT] Line {}: ERROR {} | {}", lineNo, err, ctx, ex);
                }
            }
            }
        } catch (Exception e) {
            log.error("[IMPORT] Failed to read CSV file='{}' | event='{}'", filename, eventName, e);
            return MigrationResult.error("Failed to read CSV: " + e.getMessage());
        }

        log.info("[IMPORT] Completed importVentasYa | file='{}' | event='{}' | created={} | skipped={} | duplicates={} | errors={}", filename, eventName, created, skipped, duplicates, errors);
        return new MigrationResult(created, skipped, duplicates, errors, messages);
    }

    private static BigDecimal toSafeMoney(Double val) {
        if (val == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        // Guard against NaN/Infinity
        if (val.isNaN() || val.isInfinite()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal bd = BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP);
        // DB columns use precision=10, scale=2 -> max absolute 99,999,999.99
        BigDecimal max = new BigDecimal("99999999.99");
        BigDecimal min = max.negate();
        if (bd.compareTo(max) > 0) return max;
        if (bd.compareTo(min) < 0) return min;
        return bd;
    }

    private static boolean looksLikeHeader(String raw) {
        String normalized = raw.toLowerCase(Locale.ROOT);
        return normalized.contains("codigo") && normalized.contains("descripcion");
    }

    private static String[] splitFlexible(String line) {
        // Prefer comma if present; else try tab; else collapse multiple spaces; finally, fallback to any whitespace
        String[] out;
        if (line.contains(",")) {
            out = Arrays.stream(line.split(","))
                    .map(String::trim)
                    .toArray(String[]::new);
        } else if (line.contains("\t")) {
            out = Arrays.stream(line.split("\t+"))
                    .map(String::trim)
                    .toArray(String[]::new);
        } else {
            String[] parts = Arrays.stream(line.split("\\s{2,}")) // 2+ spaces as delimiter
                    .map(String::trim)
                    .toArray(String[]::new);
            if (parts.length <= 1 && line.contains(" ")) {
                // Fallback when data is single-space separated
                String[] fallback = Arrays.stream(line.split("\\s+"))
                        .map(String::trim)
                        .toArray(String[]::new);
                if (fallback.length > parts.length) {
                    if (log.isTraceEnabled()) {
                        log.trace("[IMPORT] splitFlexible fallback to single-space split. Before parts={}, after parts={}", parts.length, fallback.length);
                    }
                    out = fallback;
                } else {
                    out = parts;
                }
            } else {
                out = parts;
            }
        }
        // Expand any embedded tab-separated values inside previously split tokens (e.g., when CSV uses commas but inner columns use tabs)
        out = flattenEmbeddedTabs(out);
        // Normalize case where first token contains "<barcode><space><description>" and the rest columns follow,
        // which typically happens when prices are all $0 and separated by 2+ spaces.
        out = normalizeLeadingBarcodeAndDesc(out);
        return out;
    }

    private static String[] normalizeLeadingBarcodeAndDesc(String[] parts) {
        if (parts == null || parts.length == 0) return parts;
        String first = parts[0];
        if (first == null) return parts;
        // Match: digits (5-30) then at least one space then some text
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("^(\\d{5,30})\\s+(.+)$");
        java.util.regex.Matcher m = p.matcher(first);
        if (m.matches()) {
            String maybeBarcode = m.group(1);
            String maybeDesc = m.group(2).trim();
            // Only split if the supposed barcode matches our global rule
            if (maybeBarcode.matches(BARCODE_REGEX)) {
                String[] rebuilt = new String[parts.length + 1];
                rebuilt[0] = maybeBarcode;
                rebuilt[1] = maybeDesc;
                // Shift the rest by 1 position
                System.arraycopy(parts, 1, rebuilt, 2, parts.length - 1);
                if (log.isDebugEnabled()) {
                    log.debug("[IMPORT] Normalized leading token combining barcode+desc -> barcode='{}' desc='{}'", maybeBarcode, maybeDesc);
                }
                return rebuilt;
            }
        }
        return parts;
    }

    private static String[] flattenEmbeddedTabs(String[] parts) {
        if (parts == null || parts.length == 0) return parts;
        boolean anyTabbed = false;
        List<String> flat = new ArrayList<>();
        for (String token : parts) {
            if (token != null && token.contains("\t")) {
                anyTabbed = true;
                String[] subs = token.split("\t+");
                for (String s : subs) {
                    if (s != null) {
                        String t = s.trim();
                        if (!t.isEmpty()) flat.add(t);
                    }
                }
            } else {
                flat.add(token);
            }
        }
        if (anyTabbed && log.isDebugEnabled()) {
            log.debug("[IMPORT] Expanded embedded tab-separated columns: before parts={} after parts={}", parts.length, flat.size());
        }
        return flat.toArray(new String[0]);
    }

    private static String safeGet(String[] arr, int idx) {
        return idx < arr.length ? arr[idx] : null;
    }

    private static String findPrecioCosto(String[] cols) {
        // Heuristic: first money-like token
        for (String c : cols) {
            if (looksLikeMoney(c)) return c;
        }
        return null;
    }

    private static String findPrecioVenta(String[] cols) {
        // Heuristic: second money-like token
        boolean firstFound = false;
        for (String c : cols) {
            if (looksLikeMoney(c)) {
                if (!firstFound) {
                    firstFound = true; // Costo
                } else {
                    return c; // Venta
                }
            }
        }
        return null;
    }

    private static boolean looksLikeMoney(String s) {
        if (s == null) return false;
        String t = s.trim().toUpperCase(Locale.ROOT);
        if (t.equals("N/A")) return true;
        // Accept formats like $0, $1.700, 1.700, 1500, $35.000
        return t.matches("^\\$?[0-9]{1,3}(\\.[0-9]{3})*(,[0-9]+)?$") || t.matches("^\\$?[0-9]+(,[0-9]+)?$");
    }

    private static Optional<Double> parseMoneyToDouble(String s) {
        if (s == null) return Optional.empty();
        String t = s.trim().toUpperCase(Locale.ROOT);
        if (t.isEmpty() || t.equals("N/A")) return Optional.of(0.0);
        // Remove currency symbols and thousand separators; handle comma decimals if present
        t = t.replace("$", "").replace(" ", "");
        if (t.contains(",")) {
            t = t.replace(".", "");
            t = t.replace(",", ".");
        } else {
            t = t.replace(".", "");
        }
        try {
            return Optional.of(Double.parseDouble(t));
        } catch (NumberFormatException e) {
            return Optional.of(0.0);
        }
    }

    private static Charset detectCharset(byte[] bytes) {
        try {
            if (bytes == null || bytes.length == 0) return StandardCharsets.UTF_8;
            // UTF-8 BOM
            if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
                return StandardCharsets.UTF_8;
            }
            // Candidates: UTF-8, Windows-1252, ISO-8859-1
            Charset win1252 = Charset.forName("Windows-1252");
            Charset iso88591 = Charset.forName("ISO-8859-1");
            List<Charset> candidates = Arrays.asList(StandardCharsets.UTF_8, win1252, iso88591);
            Charset best = StandardCharsets.UTF_8;
            int bestScore = Integer.MAX_VALUE;
            Map<String, Integer> scores = new LinkedHashMap<>();
            for (Charset cs : candidates) {
                String text = new String(bytes, cs);
                int score = scoreTextForEncodingIssues(text);
                scores.put(cs.name(), score);
                if (score < bestScore) {
                    bestScore = score;
                    best = cs;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("[IMPORT] Charset scores: {} | selected={} (score={})", scores, best.name(), bestScore);
            }
            return best;
        } catch (Exception e) {
            return StandardCharsets.UTF_8;
        }
    }

    private static int scoreTextForEncodingIssues(String text) {
        if (text == null || text.isEmpty()) return 0;
        int score = 0;
        // Replacement character count (�)
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\uFFFD') score += 10; // heavy penalty
        }
        // Common mojibake sequences when UTF-8 is decoded as Latin-1
        score += countOccurrences(text, "Ã") * 3;
        score += countOccurrences(text, "Â") * 2;
        score += countOccurrences(text, "�") * 5; // literal replacement in some renderings
        return score;
    }

    private static int countOccurrences(String haystack, String needle) {
        if (needle == null || needle.isEmpty() || haystack == null || haystack.isEmpty()) return 0;
        int idx = 0, cnt = 0;
        while ((idx = haystack.indexOf(needle, idx)) != -1) {
            cnt++;
            idx += needle.length();
        }
        return cnt;
    }
}
