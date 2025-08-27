package com.licensing.config.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test security configuration that disables all security for integration tests.
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "security.jwt.enabled", havingValue = "false")
public class TestSecurityConfiguration {

  @Bean
  public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
        .csrf(csrf -> csrf.disable())
        .headers(headers -> headers.frameOptions(options -> options.disable()))
        .build();
  }
}
