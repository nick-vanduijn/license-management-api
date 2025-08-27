package com.licensing.config.performance;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration for JVM and application performance optimization.
 * Provides recommendations and validations for production deployment.
 */
@Configuration
@ConfigurationProperties(prefix = "license.performance")
public class PerformanceConfiguration {

  private JvmConfig jvm = new JvmConfig();
  private ThreadPoolConfig threadPool = new ThreadPoolConfig();
  private ConnectionPoolConfig connectionPool = new ConnectionPoolConfig();

  @PostConstruct
  public void validateConfiguration() {
    validateJvmSettings();
    validateThreadPoolSettings();
    validateConnectionPoolSettings();
  }

  private void validateJvmSettings() {
    Runtime runtime = Runtime.getRuntime();
    long maxMemory = runtime.maxMemory() / 1024 / 1024;

    if (maxMemory < jvm.getMinHeapSizeMb()) {
      throw new IllegalStateException(
          String.format("JVM max heap size (%d MB) is below recommended minimum (%d MB). " +
              "Set -Xmx%dm or higher", maxMemory, jvm.getMinHeapSizeMb(), jvm.getMinHeapSizeMb()));
    }

    if (maxMemory > jvm.getMaxHeapSizeMb()) {
      System.out.println(
          String.format("WARNING: JVM max heap size (%d MB) exceeds recommended maximum (%d MB). " +
              "Consider setting -Xmx%dm for optimal performance",
              maxMemory, jvm.getMaxHeapSizeMb(), jvm.getMaxHeapSizeMb()));
    }
  }

  private void validateThreadPoolSettings() {
    int availableProcessors = Runtime.getRuntime().availableProcessors();

    if (threadPool.getCorePoolSize() > availableProcessors * 2) {
      System.out.println(
          String.format("WARNING: Core thread pool size (%d) is larger than 2x available processors (%d). " +
              "Consider reducing to %d",
              threadPool.getCorePoolSize(), availableProcessors, availableProcessors * 2));
    }
  }

  private void validateConnectionPoolSettings() {
    if (connectionPool.getMaxPoolSize() < connectionPool.getMinPoolSize()) {
      throw new IllegalStateException(
          String.format("Database max pool size (%d) cannot be smaller than min pool size (%d)",
              connectionPool.getMaxPoolSize(), connectionPool.getMinPoolSize()));
    }
  }

  public String getOptimalJvmSettings() {
    return String.format(
        "Recommended JVM settings for production:\n" +
            "-Xms%dm -Xmx%dm\n" +
            "-XX:+UseG1GC\n" +
            "-XX:MaxGCPauseMillis=200\n" +
            "-XX:+UseStringDeduplication\n" +
            "-XX:+PrintGCDetails\n" +
            "-XX:+PrintGCTimeStamps\n" +
            "-Djava.awt.headless=true\n" +
            "-Dserver.tomcat.threads.max=%d\n" +
            "-Dspring.datasource.hikari.maximum-pool-size=%d",
        jvm.getMinHeapSizeMb(), jvm.getRecommendedHeapSizeMb(),
        threadPool.getMaxPoolSize(), connectionPool.getMaxPoolSize());
  }

  public JvmConfig getJvm() {
    return jvm;
  }

  public void setJvm(JvmConfig jvm) {
    this.jvm = jvm;
  }

  public ThreadPoolConfig getThreadPool() {
    return threadPool;
  }

  public void setThreadPool(ThreadPoolConfig threadPool) {
    this.threadPool = threadPool;
  }

  public ConnectionPoolConfig getConnectionPool() {
    return connectionPool;
  }

  public void setConnectionPool(ConnectionPoolConfig connectionPool) {
    this.connectionPool = connectionPool;
  }

  public static class JvmConfig {
    private int minHeapSizeMb = 512;
    private int recommendedHeapSizeMb = 2048;
    private int maxHeapSizeMb = 8192;
    private String gcAlgorithm = "G1GC";
    private int maxGcPauseMillis = 200;

    public int getMinHeapSizeMb() {
      return minHeapSizeMb;
    }

    public void setMinHeapSizeMb(int minHeapSizeMb) {
      this.minHeapSizeMb = minHeapSizeMb;
    }

    public int getRecommendedHeapSizeMb() {
      return recommendedHeapSizeMb;
    }

    public void setRecommendedHeapSizeMb(int recommendedHeapSizeMb) {
      this.recommendedHeapSizeMb = recommendedHeapSizeMb;
    }

    public int getMaxHeapSizeMb() {
      return maxHeapSizeMb;
    }

    public void setMaxHeapSizeMb(int maxHeapSizeMb) {
      this.maxHeapSizeMb = maxHeapSizeMb;
    }

    public String getGcAlgorithm() {
      return gcAlgorithm;
    }

    public void setGcAlgorithm(String gcAlgorithm) {
      this.gcAlgorithm = gcAlgorithm;
    }

    public int getMaxGcPauseMillis() {
      return maxGcPauseMillis;
    }

    public void setMaxGcPauseMillis(int maxGcPauseMillis) {
      this.maxGcPauseMillis = maxGcPauseMillis;
    }
  }

  public static class ThreadPoolConfig {
    private int corePoolSize = 10;
    private int maxPoolSize = 200;
    private int queueCapacity = 500;
    private int keepAliveSeconds = 60;

    public int getCorePoolSize() {
      return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
      this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
      return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
      this.maxPoolSize = maxPoolSize;
    }

    public int getQueueCapacity() {
      return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
      this.queueCapacity = queueCapacity;
    }

    public int getKeepAliveSeconds() {
      return keepAliveSeconds;
    }

    public void setKeepAliveSeconds(int keepAliveSeconds) {
      this.keepAliveSeconds = keepAliveSeconds;
    }
  }

  public static class ConnectionPoolConfig {
    private int minPoolSize = 5;
    private int maxPoolSize = 50;
    private int connectionTimeoutMs = 30000;
    private int idleTimeoutMs = 600000;
    private int maxLifetimeMs = 1800000;

    public int getMinPoolSize() {
      return minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
      this.minPoolSize = minPoolSize;
    }

    public int getMaxPoolSize() {
      return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
      this.maxPoolSize = maxPoolSize;
    }

    public int getConnectionTimeoutMs() {
      return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
      this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public int getIdleTimeoutMs() {
      return idleTimeoutMs;
    }

    public void setIdleTimeoutMs(int idleTimeoutMs) {
      this.idleTimeoutMs = idleTimeoutMs;
    }

    public int getMaxLifetimeMs() {
      return maxLifetimeMs;
    }

    public void setMaxLifetimeMs(int maxLifetimeMs) {
      this.maxLifetimeMs = maxLifetimeMs;
    }
  }
}
