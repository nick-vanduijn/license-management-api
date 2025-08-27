package com.licensing.config.tenant;

import org.hibernate.Session;
import org.hibernate.Filter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Aspect to automatically enable tenant filter for all JPA operations
 * when JWT authentication is disabled (test mode).
 */
@Component
@ConditionalOnProperty(name = "security.jwt.enabled", havingValue = "false")
public class TenantFilterAspect {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Enables the tenant filter for the current session if a tenant context is
   * available.
   */
  public void enableTenantFilterIfNeeded() {
    String currentTenant = TenantContext.getCurrentTenant();
    if (currentTenant != null) {
      Session session = entityManager.unwrap(Session.class);
      Filter filter = session.getEnabledFilter("tenantFilter");
      if (filter == null) {
        session.enableFilter("tenantFilter").setParameter("tenantId", currentTenant);
      }
    }
  }
}
