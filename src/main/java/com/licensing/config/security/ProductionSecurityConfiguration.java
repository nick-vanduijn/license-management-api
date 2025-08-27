package com.licensing.config.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

/**
 * Production-ready security configuration with comprehensive security headers
 * and CORS configuration
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "production")
public class ProductionSecurityConfiguration {

  @Value("${license.security.cors.allowed-origins:}")
  private String[] allowedOrigins;

  @Value("${license.security.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
  private String[] allowedMethods;

  @Value("${license.security.cors.allowed-headers:*}")
  private String[] allowedHeaders;

  @Value("${license.security.cors.allow-credentials:true}")
  private boolean allowCredentials;

  @Value("${license.security.cors.max-age:3600}")
  private long maxAge;

  @Bean
  public SecurityFilterChain productionSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions.deny())
            .contentTypeOptions(contentTypeOptions -> {
            })
            .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                .maxAgeInSeconds(31536000)
                .includeSubDomains(true)
                .preload(true))
            .referrerPolicy(referrerPolicy -> referrerPolicy
                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            .addHeaderWriter((request, response) -> {
              response.setHeader("X-Content-Type-Options", "nosniff");
              response.setHeader("X-Frame-Options", "DENY");
              response.setHeader("X-XSS-Protection", "1; mode=block");
              response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
              response.setHeader("Pragma", "no-cache");
              response.setHeader("Expires", "0");
              response.setHeader("Server", "");
              response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=(), payment=()");
            }))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .requestMatchers("/api/v1/auth/**").permitAll()
            .anyRequest().authenticated());

    return http.build();
  }

  @Bean
  @ConditionalOnProperty(name = "license.security.cors.enabled", havingValue = "true", matchIfMissing = true)
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    if (allowedOrigins.length > 0) {
      configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
    } else {
      configuration.setAllowedOriginPatterns(List.of("*"));
    }

    configuration.setAllowedMethods(Arrays.asList(allowedMethods));
    configuration.setAllowedHeaders(Arrays.asList(allowedHeaders));
    configuration.setAllowCredentials(allowCredentials);
    configuration.setMaxAge(maxAge);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
