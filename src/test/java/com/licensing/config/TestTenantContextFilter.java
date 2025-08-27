package com.licensing.config;

import com.licensing.config.tenant.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;

/**
 * Test-only filter that sets tenant context from X-Tenant-ID header
 * when JWT authentication is disabled.
 */
@Component
@ConditionalOnProperty(name = "security.jwt.enabled", havingValue = "false")
@Order(1)
public class TestTenantContextFilter implements Filter {

  private static final String TENANT_HEADER = "X-Tenant-ID";

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    if (request instanceof HttpServletRequest httpRequest) {
      String tenantId = httpRequest.getHeader(TENANT_HEADER);

      if (tenantId != null && !tenantId.trim().isEmpty()) {
        TenantContext.setCurrentTenant(tenantId);

        try {
          Session session = entityManager.unwrap(Session.class);
          session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        } catch (Exception e) {
          throw new ServletException("Failed to enable tenant filter", e);
        }
      }
    }

    try {
      chain.doFilter(request, response);
    } finally {
      TenantContext.clear();
    }
  }
}
