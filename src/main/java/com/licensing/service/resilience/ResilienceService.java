package com.licensing.service.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

/**
 * Service providing resilience patterns for critical operations.
 * Wraps database and external service calls with circuit breaker, retry, and
 * timeout protection.
 */
@Service
public class ResilienceService {

  private static final Logger logger = LoggerFactory.getLogger(ResilienceService.class);

  private final CircuitBreaker databaseCircuitBreaker;
  private final CircuitBreaker redisCircuitBreaker;
  private final CircuitBreaker externalApiCircuitBreaker;
  private final Retry databaseRetry;
  private final Retry redisRetry;
  private final TimeLimiter databaseTimeLimiter;
  private final TimeLimiter externalApiTimeLimiter;
  private final ScheduledExecutorService scheduler;

  public ResilienceService(CircuitBreakerRegistry circuitBreakerRegistry,
      RetryRegistry retryRegistry,
      TimeLimiterRegistry timeLimiterRegistry) {
    this.databaseCircuitBreaker = circuitBreakerRegistry.circuitBreaker("database");
    this.redisCircuitBreaker = circuitBreakerRegistry.circuitBreaker("redis");
    this.externalApiCircuitBreaker = circuitBreakerRegistry.circuitBreaker("external-api");
    this.databaseRetry = retryRegistry.retry("database");
    this.redisRetry = retryRegistry.retry("redis");
    this.databaseTimeLimiter = timeLimiterRegistry.timeLimiter("database");
    this.externalApiTimeLimiter = timeLimiterRegistry.timeLimiter("external-api");
    this.scheduler = Executors.newScheduledThreadPool(4);

    setupEventListeners();
  }

  /**
   * Executes database operations with circuit breaker, retry, and timeout
   * protection.
   */
  public <T> T executeDatabaseOperation(Supplier<T> operation, Supplier<T> fallback) {
    Supplier<T> decoratedSupplier = CircuitBreaker
        .decorateSupplier(databaseCircuitBreaker, operation);

    decoratedSupplier = Retry.decorateSupplier(databaseRetry, decoratedSupplier);

    try {
      return decoratedSupplier.get();
    } catch (Exception e) {
      logger.warn("Database operation failed, using fallback: {}", e.getMessage());
      return fallback.get();
    }
  }

  /**
   * Executes database operations with full protection including timeout.
   */
  public <T> CompletableFuture<T> executeDatabaseOperationWithTimeout(
      Supplier<CompletableFuture<T>> operation, Supplier<T> fallback) {

    Supplier<CompletableFuture<T>> decoratedSupplier = CircuitBreaker
        .decorateSupplier(databaseCircuitBreaker, operation);

    try {
      CompletableFuture<T> future = decoratedSupplier.get();
      return timeLimitedFuture(future, databaseTimeLimiter);
    } catch (Exception e) {
      logger.warn("Database operation with timeout failed, using fallback: {}", e.getMessage());
      return CompletableFuture.completedFuture(fallback.get());
    }
  }

  /**
   * Executes Redis operations with circuit breaker and retry protection.
   * Redis operations should fail fast, so no timeout is applied.
   */
  public <T> T executeRedisOperation(Supplier<T> operation, Supplier<T> fallback) {
    Supplier<T> decoratedSupplier = CircuitBreaker
        .decorateSupplier(redisCircuitBreaker, operation);

    decoratedSupplier = Retry.decorateSupplier(redisRetry, decoratedSupplier);

    try {
      return decoratedSupplier.get();
    } catch (Exception e) {
      logger.warn("Redis operation failed, using fallback: {}", e.getMessage());
      return fallback.get();
    }
  }

  /**
   * Executes external API calls with full resilience protection.
   */
  public <T> CompletableFuture<T> executeExternalApiCall(Supplier<CompletableFuture<T>> operation,
      Supplier<T> fallback) {
    Supplier<CompletableFuture<T>> decoratedSupplier = CircuitBreaker
        .decorateSupplier(externalApiCircuitBreaker, operation);

    try {
      CompletableFuture<T> future = decoratedSupplier.get();
      return timeLimitedFuture(future, externalApiTimeLimiter);
    } catch (Exception e) {
      logger.warn("External API call failed, using fallback: {}", e.getMessage());
      return CompletableFuture.completedFuture(fallback.get());
    }
  }

  /**
   * Executes any operation with specific circuit breaker protection.
   */
  public <T> T executeWithCircuitBreaker(String circuitBreakerName, Supplier<T> operation,
      Supplier<T> fallback) {
    CircuitBreaker circuitBreaker = getCircuitBreakerByName(circuitBreakerName);

    Supplier<T> decoratedSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, operation);

    try {
      return decoratedSupplier.get();
    } catch (Exception e) {
      logger.warn("Operation with circuit breaker '{}' failed, using fallback: {}",
          circuitBreakerName, e.getMessage());
      return fallback.get();
    }
  }

  /**
   * Gets circuit breaker metrics for monitoring.
   */
  public String getCircuitBreakerStatus(String circuitBreakerName) {
    CircuitBreaker circuitBreaker = getCircuitBreakerByName(circuitBreakerName);
    CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();

    return String.format(
        "CircuitBreaker '%s': State=%s, FailureRate=%.2f%%, NumberOfCalls=%d, NumberOfFailedCalls=%d",
        circuitBreakerName,
        circuitBreaker.getState(),
        metrics.getFailureRate(),
        metrics.getNumberOfBufferedCalls(),
        metrics.getNumberOfFailedCalls());
  }

  private CircuitBreaker getCircuitBreakerByName(String name) {
    return switch (name) {
      case "database" -> databaseCircuitBreaker;
      case "redis" -> redisCircuitBreaker;
      case "external-api" -> externalApiCircuitBreaker;
      default -> throw new IllegalArgumentException("Unknown circuit breaker: " + name);
    };
  }

  private <T> CompletableFuture<T> timeLimitedFuture(CompletableFuture<T> future, TimeLimiter timeLimiter) {
    return timeLimiter.executeCompletionStage(scheduler, () -> future).toCompletableFuture();
  }

  private void setupEventListeners() {
    databaseCircuitBreaker.getEventPublisher()
        .onStateTransition(event -> logger.info("Database circuit breaker state transition: {} -> {}",
            event.getStateTransition().getFromState(),
            event.getStateTransition().getToState()))
        .onCallNotPermitted(event -> logger.warn("Database circuit breaker rejected call"))
        .onFailureRateExceeded(event -> logger.error("Database circuit breaker failure rate exceeded: {}%",
            event.getFailureRate()));

    redisCircuitBreaker.getEventPublisher()
        .onStateTransition(event -> logger.info("Redis circuit breaker state transition: {} -> {}",
            event.getStateTransition().getFromState(),
            event.getStateTransition().getToState()))
        .onCallNotPermitted(event -> logger.warn("Redis circuit breaker rejected call"));

    externalApiCircuitBreaker.getEventPublisher()
        .onStateTransition(event -> logger.info("External API circuit breaker state transition: {} -> {}",
            event.getStateTransition().getFromState(),
            event.getStateTransition().getToState()))
        .onCallNotPermitted(event -> logger.warn("External API circuit breaker rejected call"));
  }
}
