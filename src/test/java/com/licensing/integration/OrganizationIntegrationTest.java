package com.licensing.integration;

import com.licensing.config.tenant.TenantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrganizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @PersistenceContext
    private EntityManager entityManager;

    @AfterEach
    void cleanUp() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should create organization for tenant")
    void shouldCreateOrganizationForTenant() throws Exception {
        String requestBody = """
                {
                    "name": "Acme Corporation",
                    "contactEmail": "admin@acme.com",
                    "plan": "ENTERPRISE"
                }
                """;

        mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Tenant-ID", "tenant-123")
                .header("X-User-ID", "user-123")
                .header("Idempotency-Key", "create-org-001")
                .header("Authorization", "Bearer test-api-key")
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Acme Corporation"))
                .andExpect(jsonPath("$.contactEmail").value("admin@acme.com"))
                .andExpect(jsonPath("$.plan").value("ENTERPRISE"))
                .andExpect(jsonPath("$.tenantId").value("tenant-123"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Should fail when tenant header is missing")
    void shouldFailWhenTenantHeaderMissing() throws Exception {
        String requestBody = """
                {
                    "name": "Test Corp",
                    "contactEmail": "test@corp.com",
                    "plan": "BASIC"
                }
                """;

        mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "create-org-002")
                .header("Authorization", "Bearer test-api-key")
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MISSING_TENANT"))
                .andExpect(jsonPath("$.message").value(containsString("X-User-ID")));
    }
}
