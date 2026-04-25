package com.infinitesoft.pos_relational_data_service.monitoring;

/**
 * Static bridge so log4j2 plugins (instantiated by log4j2's own factory, NOT
 * by Spring) can hand off log lines to the Spring-managed {@link InfluxWriter}.
 *
 * <p>Same pattern as the legacy {@code DbAppender} -> {@code Log4jDataSourceConfig}
 * setup: Spring sets the writer once it's ready; the appender silently drops
 * events while Spring isn't up yet.
 */
public final class InfluxStaticHolder {

    private static volatile InfluxWriter writer;

    private InfluxStaticHolder() {}

    public static void set(InfluxWriter w) { writer = w; }

    public static InfluxWriter get() { return writer; }
}
