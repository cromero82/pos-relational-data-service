package com.infinitesoft.pos_relational_data_service.monitoring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Properties bound to {@code monitor.influx.*} in application.properties.
 * <p>
 * Values are read once at startup and used by {@link InfluxWriter},
 * {@link InfluxConfig} and {@link InfluxBootstrap}.
 */
@Component
@ConfigurationProperties(prefix = "monitor.influx")
public class InfluxProperties {

    private String url            = "http://127.0.0.1:8181";
    private String database       = "infinito_logs";
    private String retention      = "90d";
    private int    batchSize      = 100;
    private long   flushIntervalMs = 2000L;
    private int    queueCapacity  = 10000;
    private String spoolDir       = "./monitor-spool";
    private long   retryIntervalMs = 30000L;
    private long   httpTimeoutMs  = 5000L;
    /** Empty when InfluxDB runs with --without-auth. Otherwise: "Token <admin-token>". */
    private String authHeader     = "";

    public String getUrl()             { return url; }
    public void   setUrl(String url)   { this.url = url; }

    public String getDatabase()                 { return database; }
    public void   setDatabase(String database)  { this.database = database; }

    public String getRetention()               { return retention; }
    public void   setRetention(String s)       { this.retention = s; }

    public int  getBatchSize()         { return batchSize; }
    public void setBatchSize(int n)    { this.batchSize = n; }

    public long getFlushIntervalMs()         { return flushIntervalMs; }
    public void setFlushIntervalMs(long ms)  { this.flushIntervalMs = ms; }

    public int  getQueueCapacity()         { return queueCapacity; }
    public void setQueueCapacity(int n)    { this.queueCapacity = n; }

    public String getSpoolDir()              { return spoolDir; }
    public void   setSpoolDir(String dir)    { this.spoolDir = dir; }

    public long getRetryIntervalMs()         { return retryIntervalMs; }
    public void setRetryIntervalMs(long ms)  { this.retryIntervalMs = ms; }

    public long getHttpTimeoutMs()           { return httpTimeoutMs; }
    public void setHttpTimeoutMs(long ms)    { this.httpTimeoutMs = ms; }

    public String getAuthHeader()            { return authHeader; }
    public void   setAuthHeader(String s)    { this.authHeader = s; }
}
