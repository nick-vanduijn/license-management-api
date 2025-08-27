package com.licensing.config.logging;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Configuration for request/response logging filter.
 * Registers the filter with proper ordering for production monitoring.
 */
@Configuration
public class LoggingConfiguration {

  private final RequestResponseLoggingFilter requestResponseLoggingFilter;

  public LoggingConfiguration(RequestResponseLoggingFilter requestResponseLoggingFilter) {
    this.requestResponseLoggingFilter = requestResponseLoggingFilter;
  }

  @Bean
  public FilterRegistrationBean<RequestResponseLoggingFilter> loggingFilter() {
    FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean = new FilterRegistrationBean<>();

    registrationBean.setFilter(requestResponseLoggingFilter);
    registrationBean.addUrlPatterns("/api/*");
    registrationBean.setName("requestResponseLoggingFilter");
    registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);

    return registrationBean;
  }
}
