package com.licensing.config.info;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Custom info contributor for application metadata
 */
@Component
public class CustomInfoContributor implements InfoContributor {

    @Value("${spring.application.name:License Management API}")
    private String applicationName;

    @Value("${application.version:1.0.0-SNAPSHOT}")
    private String version;

    @Value("${license.api.features.rate-limiting:false}")
    private boolean rateLimitingEnabled;

    @Value("${license.security.jwt.enabled:true}")
    private boolean jwtEnabled;

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("application", Map.of(
                "name", applicationName,
                "version", version,
                "startup-time", Instant.now(),
                "description", "Production-ready multi-tenant licensing API"));

        builder.withDetail("features", Map.of(
                "multi-tenancy", true,
                "jwt-authentication", jwtEnabled,
                "rate-limiting", rateLimitingEnabled,
                "digital-signatures", true,
                "audit-logging", true,
                "metrics", true));

        builder.withDetail("security", Map.of(
                "authentication", "JWT Bearer Token",
                "authorization", "Role-based + Tenant isolation",
                "encryption", "Ed25519 digital signatures",
                "headers", "Comprehensive security headers"));

        builder.withDetail("api", Map.of(
                "version", "v1",
                "documentation", "/swagger-ui.html",
                "openapi-spec", "/v3/api-docs",
                "base-path", "/api/v1"));

        builder.withDetail("timestamp", Instant.now());
        builder.withDetail("uptime", System.currentTimeMillis() - Instant.now().toEpochMilli());
        builder.withDetail("environment", System.getenv());
        builder.withDetail("system-properties", System.getProperties());
        builder.withDetail("application-name", applicationName);
        builder.withDetail("version", version);
        builder.withDetail("rate-limiting-enabled", rateLimitingEnabled);
        builder.withDetail("jwt-enabled", jwtEnabled);
    }
}
