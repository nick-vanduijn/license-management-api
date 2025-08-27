package com.licensing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for the License Management API.
 * Provides comprehensive API documentation with security schemes and server
 * information.
 */
@Configuration
public class OpenApiConfiguration {

  @Value("${app.version:1.0.0}")
  private String appVersion;

  @Value("${server.port:8080}")
  private String serverPort;

  @Bean
  public OpenAPI licenseManagementOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("License Management API")
            .description("Production-grade multi-tenant licensing API for software license management, " +
                "validation, and cryptographic signing. Supports complete license lifecycle management " +
                "with tenant isolation and secure token generation.")
            .version(appVersion)
            .contact(new Contact()
                .name("License Management Team")
                .email("support@licensing.com")
                .url("https://licensing.com/support"))
            .license(new License()
                .name("MIT")
                .url("https://opensource.org/licenses/MIT")))
        .servers(List.of(
            new Server()
                .url("http://localhost:" + serverPort)
                .description("Development server"),
            new Server()
                .url("https://api.licensing.com")
                .description("Production server")))
        .addSecurityItem(new SecurityRequirement()
            .addList("TenantHeader"))
        .components(new io.swagger.v3.oas.models.Components()
            .addSecuritySchemes("TenantHeader", new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-Tenant-ID")
                .description("Tenant identifier required for multi-tenant operations")));
  }
}
