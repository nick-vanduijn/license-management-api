package com.licensing.config.logging;

import com.licensing.config.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Filter that logs HTTP requests and responses for monitoring and debugging.
 * Captures request/response data and performance metrics.
 */
@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
  private static final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE");

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    if (shouldNotLog(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    String correlationId = UUID.randomUUID().toString();
    ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

    long startTime = System.currentTimeMillis();

    try {

      MDC.put("correlationId", correlationId);

      logRequestDetails(wrappedRequest, correlationId);

      filterChain.doFilter(wrappedRequest, wrappedResponse);

    } finally {
      long duration = System.currentTimeMillis() - startTime;
      logResponseDetails(wrappedResponse, correlationId, duration);
      logPerformanceMetrics(wrappedRequest, wrappedResponse, duration);

      wrappedResponse.copyBodyToResponse();

      MDC.remove("correlationId");
    }
  }

  private void logRequestDetails(ContentCachingRequestWrapper request, String correlationId) {
    String method = request.getMethod();
    String uri = request.getRequestURI();
    String queryString = request.getQueryString();
    String userAgent = request.getHeader("User-Agent");
    String clientIp = getClientIpAddress(request);

    logger.info("Incoming request - Method: {}, URI: {}, Query: {}, IP: {}, UserAgent: {}, CorrelationId: {}",
        method, uri, queryString, clientIp, userAgent, correlationId);

    if (shouldLogRequestBody(request)) {
      String requestBody = getRequestBody(request);
      if (requestBody != null && !requestBody.isEmpty()) {
        logger.debug("Request body - CorrelationId: {}, Body: {}", correlationId, requestBody);
      }
    }
  }

  private void logResponseDetails(ContentCachingResponseWrapper response, String correlationId, long duration) {
    int status = response.getStatus();
    String contentType = response.getContentType();

    logger.info("Outgoing response - Status: {}, Duration: {}ms, ContentType: {}, CorrelationId: {}",
        status, duration, contentType, correlationId);

    if (status >= 400) {
      String responseBody = getResponseBody(response);
      if (responseBody != null && !responseBody.isEmpty()) {
        logger.error("Error response body - CorrelationId: {}, Status: {}, Body: {}",
            correlationId, status, responseBody);
      }
    }
  }

  private void logPerformanceMetrics(ContentCachingRequestWrapper request,
      ContentCachingResponseWrapper response, long duration) {
    String method = request.getMethod();
    String uri = request.getRequestURI();
    int status = response.getStatus();
    long requestSize = request.getContentLength();
    long responseSize = response.getContentSize();

    performanceLogger.info("method={} uri={} status={} duration={}ms requestSize={} responseSize={} tenantId={}",
        method, uri, status, duration, requestSize, responseSize, getCurrentTenantId());

    if (duration > 5000) {
      logger.warn("Slow request detected - URI: {}, Duration: {}ms", uri, duration);
    }

    if (responseSize > 1024 * 1024) {
      logger.warn("Large response detected - URI: {}, Size: {} bytes", uri, responseSize);
    }
  }

  private boolean shouldNotLog(HttpServletRequest request) {
    String uri = request.getRequestURI();
    return uri.startsWith("/actuator/") ||
        uri.startsWith("/v3/api-docs") ||
        uri.startsWith("/swagger-ui") ||
        uri.equals("/favicon.ico");
  }

  private boolean shouldLogRequestBody(ContentCachingRequestWrapper request) {
    String method = request.getMethod();
    String uri = request.getRequestURI();

    if (!("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method))) {
      return false;
    }

    return !uri.contains("/auth/") && !uri.contains("/login") && !uri.contains("/password");
  }

  private String getRequestBody(ContentCachingRequestWrapper request) {
    try {
      byte[] content = request.getContentAsByteArray();
      if (content.length > 0) {
        return new String(content, StandardCharsets.UTF_8);
      }
    } catch (Exception e) {
      logger.debug("Failed to read request body", e);
    }
    return null;
  }

  private String getResponseBody(ContentCachingResponseWrapper response) {
    try {
      byte[] content = response.getContentAsByteArray();
      if (content.length > 0) {
        return new String(content, StandardCharsets.UTF_8);
      }
    } catch (Exception e) {
      logger.debug("Failed to read response body", e);
    }
    return null;
  }

  private String getClientIpAddress(HttpServletRequest request) {
    String[] headers = { "X-Forwarded-For", "X-Real-IP", "X-Client-IP", "X-Forwarded" };

    for (String header : headers) {
      String ip = request.getHeader(header);
      if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {

        return ip.split(",")[0].trim();
      }
    }

    return request.getRemoteAddr();
  }

  private String getCurrentTenantId() {
    try {
      return TenantContext.getCurrentTenant();
    } catch (Exception e) {
      return "unknown";
    }
  }
}
