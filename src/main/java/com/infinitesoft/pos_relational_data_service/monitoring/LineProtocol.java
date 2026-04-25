package com.infinitesoft.pos_relational_data_service.monitoring;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper to build InfluxDB v3 line-protocol strings.
 * <pre>
 *   measurement,tag1=v1,tag2=v2 field1="text",field2=42 1700000000000000000
 * </pre>
 *
 * Escaping rules:
 * <ul>
 *   <li>Tag keys/values and field keys: escape commas, equal signs and spaces.</li>
 *   <li>Field string values (always wrapped in double quotes): escape backslash
 *       and double quote; newlines become {@code \n}.</li>
 * </ul>
 */
public final class LineProtocol {

    private LineProtocol() {}

    /** Builder for a single line. Tags inserted before fields, in insertion order. */
    public static final class Line {
        private final String measurement;
        private final Map<String, String> tags   = new LinkedHashMap<>();
        private final Map<String, String> fields = new LinkedHashMap<>();
        private long timestampNanos = -1L;

        public Line(String measurement) {
            this.measurement = escapeMeasurement(measurement);
        }

        /** Tag - indexed; null/empty values are skipped. */
        public Line tag(String key, String value) {
            if (key == null || value == null || value.isEmpty()) return this;
            tags.put(escapeTag(key), escapeTag(value));
            return this;
        }

        /** String field (most common). null values are skipped. */
        public Line field(String key, String value) {
            if (key == null || value == null) return this;
            fields.put(escapeTag(key), "\"" + escapeFieldString(value) + "\"");
            return this;
        }

        /** Long field. Influx requires integer fields suffixed with {@code i}. */
        public Line field(String key, long value) {
            fields.put(escapeTag(key), value + "i");
            return this;
        }

        /** Double field. */
        public Line field(String key, double value) {
            fields.put(escapeTag(key), Double.toString(value));
            return this;
        }

        /** Boolean field. */
        public Line field(String key, boolean value) {
            fields.put(escapeTag(key), Boolean.toString(value));
            return this;
        }

        public Line timestampNanos(long nanos) {
            this.timestampNanos = nanos;
            return this;
        }

        /** Convenience: timestamp from java {@code System.currentTimeMillis()}. */
        public Line timestampMillis(long millis) {
            this.timestampNanos = millis * 1_000_000L;
            return this;
        }

        @Override
        public String toString() {
            if (fields.isEmpty()) {
                throw new IllegalStateException("Line must have at least one field: " + measurement);
            }
            StringBuilder sb = new StringBuilder(128);
            sb.append(measurement);
            for (Map.Entry<String, String> e : tags.entrySet()) {
                sb.append(',').append(e.getKey()).append('=').append(e.getValue());
            }
            sb.append(' ');
            boolean first = true;
            for (Map.Entry<String, String> e : fields.entrySet()) {
                if (!first) sb.append(',');
                sb.append(e.getKey()).append('=').append(e.getValue());
                first = false;
            }
            if (timestampNanos > 0) {
                sb.append(' ').append(timestampNanos);
            }
            return sb.toString();
        }
    }

    // ---- escapes ----------------------------------------------------------

    static String escapeMeasurement(String s) {
        // measurement: escape comma and space
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ',' || c == ' ') out.append('\\');
            out.append(c);
        }
        return out.toString();
    }

    static String escapeTag(String s) {
        // tag key, tag value, field key: escape comma, equal sign and space
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ',' || c == '=' || c == ' ') out.append('\\');
            out.append(c);
        }
        return out.toString();
    }

    static String escapeFieldString(String s) {
        // string field value (between quotes): escape backslash and double quote;
        // also normalize newlines to literal \n so the line protocol stays single-line.
        StringBuilder out = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': out.append("\\\\"); break;
                case '"' : out.append("\\\""); break;
                case '\n': out.append("\\n");  break;
                case '\r': out.append("\\r");  break;
                default  : out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * Trims a tag value to {@code maxLen} chars and replaces sequences not allowed
     * (for clean indexing). Useful for thread names / class names in tags.
     */
    public static String safeTag(String value, int maxLen) {
        if (value == null) return null;
        String v = value.length() > maxLen ? value.substring(0, maxLen) : value;
        // Replace whitespace with underscore to keep tag readable in queries
        return v.replaceAll("\\s+", "_");
    }
}
