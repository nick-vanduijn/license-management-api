package com.licensing.config.rate;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Rate limiting configuration using bucket4j
 */
@Configuration
@ConditionalOnProperty(name = "license.api.rate-limit.enabled", havingValue = "true", matchIfMissing = false)
public class RateLimitingConfiguration implements WebMvcConfigurer {

  @Value("${license.api.rate-limit.requests-per-minute:60}")
  private int requestsPerMinute;

  @Value("${license.api.rate-limit.burst-capacity:100}")
  private int burstCapacity;

  private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

  @Bean
  public RateLimitInterceptor rateLimitInterceptor() {
    return new RateLimitInterceptor();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rateLimitInterceptor())
        .addPathPatterns("/api/**")
        .excludePathPatterns("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**");
  }

  public class RateLimitInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
      String clientId = getClientId(request);
      Bucket bucket = getBucket(clientId);

      if (bucket.tryConsume(1)) {

        response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000));
        return true;
      } else {
        response.setStatus(429);
        response.setHeader("Content-Type", "application/json");
        response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000));

        try {
          response.getWriter().write("""
              {
                  "error": "RATE_LIMIT_EXCEEDED",
                  "message": "Too many requests. Please try again later.",
                  "timestamp": "%s"
              }
              """.formatted(java.time.Instant.now()));
        } catch (Exception e) {

        }
        return false;
      }
    }

    private String getClientId(HttpServletRequest request) {

      String apiKey = request.getHeader("Authorization");
      if (apiKey != null && !apiKey.isEmpty()) {
        return "api_key_" + apiKey.hashCode();
      }

      String tenantId = request.getHeader("X-Tenant-ID");
      if (tenantId != null && !tenantId.isEmpty()) {
        return "tenant_" + tenantId;
      }

      String forwardedFor = request.getHeader("X-Forwarded-For");
      if (forwardedFor != null && !forwardedFor.isEmpty()) {
        return "ip_" + forwardedFor.split(",")[0].trim();
      }

      return "ip_" + request.getRemoteAddr();
    }

    private Bucket getBucket(String clientId) {
      return buckets.computeIfAbsent(clientId, this::createBucket);
    }

    private Bucket createBucket(String clientId) {
      Bandwidth limit = Bandwidth.classic(requestsPerMinute,
          Refill.intervally(requestsPerMinute, Duration.ofMinutes(1)));
      Bandwidth burst = Bandwidth.simple(burstCapacity, Duration.ofMinutes(1));
      return Bucket.builder()
          .addLimit(limit)
          .addLimit(burst)
          .build();
    }
  }
}
