package com.licensing.config.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for Resilience4j patterns including circuit breaker,
 * retry, and time limiter for production resilience.
 */
@Configuration
public class ResilienceConfiguration {

  @Bean
  public CircuitBreakerRegistry circuitBreakerRegistry() {
    return CircuitBreakerRegistry.ofDefaults();
  }

  @Bean
  public CircuitBreaker databaseCircuitBreaker(CircuitBreakerRegistry registry) {
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
        .failureRateThreshold(50.0f)
        .waitDurationInOpenState(Duration.ofSeconds(30))
        .slidingWindowSize(10)
        .minimumNumberOfCalls(5)
        .permittedNumberOfCallsInHalfOpenState(3)
        .automaticTransitionFromOpenToHalfOpenEnabled(true)
        .build();

    return registry.circuitBreaker("database", config);
  }

  @Bean
  public CircuitBreaker redisCircuitBreaker(CircuitBreakerRegistry registry) {
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
        .failureRateThreshold(60.0f)
        .waitDurationInOpenState(Duration.ofSeconds(20))
        .slidingWindowSize(8)
        .minimumNumberOfCalls(3)
        .permittedNumberOfCallsInHalfOpenState(2)
        .automaticTransitionFromOpenToHalfOpenEnabled(true)
        .build();

    return registry.circuitBreaker("redis", config);
  }

  @Bean
  public CircuitBreaker externalApiCircuitBreaker(CircuitBreakerRegistry registry) {
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
        .failureRateThreshold(40.0f)
        .waitDurationInOpenState(Duration.ofMinutes(1))
        .slidingWindowSize(15)
        .minimumNumberOfCalls(5)
        .permittedNumberOfCallsInHalfOpenState(2)
        .automaticTransitionFromOpenToHalfOpenEnabled(true)
        .build();

    return registry.circuitBreaker("external-api", config);
  }

  @Bean
  public RetryRegistry retryRegistry() {
    return RetryRegistry.ofDefaults();
  }

  @Bean
  public Retry databaseRetry(RetryRegistry registry) {
    RetryConfig config = RetryConfig.custom()
        .maxAttempts(3)
        .waitDuration(Duration.ofMillis(100))
        .retryOnException(ex -> ex instanceof java.sql.SQLException ||
            ex instanceof org.springframework.dao.DataAccessException)
        .build();

    return registry.retry("database", config);
  }

  @Bean
  public Retry redisRetry(RetryRegistry registry) {
    RetryConfig config = RetryConfig.custom()
        .maxAttempts(2)
        .waitDuration(Duration.ofMillis(50))
        .retryOnException(ex -> ex instanceof org.springframework.data.redis.RedisConnectionFailureException)
        .build();

    return registry.retry("redis", config);
  }

  @Bean
  public TimeLimiterRegistry timeLimiterRegistry() {
    return TimeLimiterRegistry.ofDefaults();
  }

  @Bean
  public TimeLimiter databaseTimeLimiter(TimeLimiterRegistry registry) {
    TimeLimiterConfig config = TimeLimiterConfig.custom()
        .timeoutDuration(Duration.ofSeconds(3))
        .build();

    return registry.timeLimiter("database", config);
  }

  @Bean
  public TimeLimiter externalApiTimeLimiter(TimeLimiterRegistry registry) {
    TimeLimiterConfig config = TimeLimiterConfig.custom()
        .timeoutDuration(Duration.ofSeconds(10))
        .build();

    return registry.timeLimiter("external-api", config);
  }
}
