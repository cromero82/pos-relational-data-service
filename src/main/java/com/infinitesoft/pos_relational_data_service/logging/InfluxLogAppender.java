package com.infinitesoft.pos_relational_data_service.logging;

import com.infinitesoft.pos_relational_data_service.monitoring.InfluxStaticHolder;
import com.infinitesoft.pos_relational_data_service.monitoring.InfluxWriter;
import com.infinitesoft.pos_relational_data_service.monitoring.LineProtocol;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * log4j2 appender that ships every {@link LogEvent} to InfluxDB 3 as a line in
 * the {@code backend_log} measurement.
 *
 * <p>This replaces the legacy {@code DbAppender} (which did synchronous JDBC
 * INSERTs into the postgres {@code app_log} table). The actual HTTP shipping
 * is delegated to {@link InfluxWriter}; the appender just builds the line
 * and enqueues it (non-blocking).
 *
 * <p>Tags (indexed, low cardinality):
 * <ul>
 *   <li>{@code level}  - log level name (INFO, WARN, ERROR, ...)</li>
 *   <li>{@code logger} - shortened logger name (last 80 chars)</li>
 *   <li>{@code thread} - thread name (truncated)</li>
 *   <li>{@code app}    - service name, fixed = {@code pos-negocio}</li>
 * </ul>
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code message}   - rendered message (string)</li>
 *   <li>{@code exception} - full stacktrace if present (string), else absent</li>
 * </ul>
 */
@Plugin(name = "InfluxLogAppender",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE,
        printObject = true)
public final class InfluxLogAppender extends AbstractAppender {

    /** Identifies which Spring Boot service produced the log (useful when we
     *  add more services later: seguridad, smtp, etc.). */
    private static final String APP_NAME = "pos-negocio";

    private InfluxLogAppender(String name, Filter filter) {
        super(name, filter, null, true, Property.EMPTY_ARRAY);
    }

    @PluginFactory
    public static InfluxLogAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter) {
        if (name == null) {
            LOGGER.error("InfluxLogAppender requires a name attribute");
            return null;
        }
        return new InfluxLogAppender(name, filter);
    }

    @Override
    public void append(LogEvent event) {
        InfluxWriter writer = InfluxStaticHolder.get();
        if (writer == null) {
            // Spring not ready yet (early framework logs). Silently skip,
            // same behavior as the legacy DbAppender.
            return;
        }
        try {
            // exception ALWAYS present so InfluxDB 3 keeps the column in the
            // schema even for normal logs. Empty string when there's no throwable.
            // Without this the column never exists until the first error,
            // and Grafana queries that SELECT it fail with "no field named exception".
            String exceptionStr = "";
            Throwable t = event.getThrown();
            if (t != null) {
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                exceptionStr = sw.toString();
            }

            LineProtocol.Line line = new LineProtocol.Line("backend_log")
                    .tag("app",    APP_NAME)
                    .tag("level",  event.getLevel().name())
                    .tag("logger", LineProtocol.safeTag(event.getLoggerName(), 80))
                    .tag("thread", LineProtocol.safeTag(event.getThreadName(), 60))
                    .field("message",   safe(event.getMessage().getFormattedMessage()))
                    .field("exception", exceptionStr)
                    .timestampMillis(event.getTimeMillis());

            writer.enqueue(line);
        } catch (Throwable e) {
            // Appender must NEVER throw — would break user code paths.
            error("InfluxLogAppender: error building/enqueuing line", event, e);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
