package com.infinitesoft.pos_relational_data_service.logging;

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

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;

@Plugin(name = "DbAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public final class DbAppender extends AbstractAppender {

    private static final String SQL =
            "INSERT INTO app_log (fecha, nivel, logger, mensaje, excepcion, thread) VALUES (?, ?, ?, ?, ?, ?)";

    private static volatile DataSource dataSource;

    private DbAppender(String name, Filter filter) {
        super(name, filter, null, true, Property.EMPTY_ARRAY);
    }

    @PluginFactory
    public static DbAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter) {
        if (name == null) {
            LOGGER.error("DbAppender requires a name attribute");
            return null;
        }
        return new DbAppender(name, filter);
    }

    public static void setDataSource(DataSource ds) {
        dataSource = ds;
    }

    @Override
    public void append(LogEvent event) {
        DataSource ds = dataSource;
        if (ds == null) {
            return; // Spring not ready yet — silently skip
        }
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setTimestamp(1, new Timestamp(event.getTimeMillis()));
            ps.setString(2, event.getLevel().name());
            ps.setString(3, truncate(event.getLoggerName(), 255));
            ps.setString(4, event.getMessage().getFormattedMessage());
            Throwable thrown = event.getThrown();
            if (thrown != null) {
                StringWriter sw = new StringWriter();
                thrown.printStackTrace(new PrintWriter(sw));
                ps.setString(5, sw.toString());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }
            ps.setString(6, truncate(event.getThreadName(), 100));
            ps.executeUpdate();
        } catch (Exception e) {
            error("DbAppender: error writing log to DB", event, e);
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
