package com.licensing.repository;

import com.licensing.config.TestJpaConfiguration;
import com.licensing.config.tenant.TenantContext;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for repository tests that provides tenant-aware testing with H2.
 * Enables tenant filtering to simulate schema-based multi-tenancy.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfiguration.class)
public abstract class BaseRepositoryTest {

  @Autowired
  protected TestEntityManager entityManager;

  protected final String defaultTenantId = "test-tenant";

  @BeforeEach
  void setUpTenantContext() {
    TenantContext.setCurrentTenant(defaultTenantId);
    enableTenantFilter(defaultTenantId);
  }

  @AfterEach
  void tearDownTenantContext() {
    TenantContext.clear();
    disableTenantFilter();
  }

  /**
   * Switch to a different tenant and enable filtering for that tenant.
   */
  protected void switchToTenant(String tenantId) {
    TenantContext.setCurrentTenant(tenantId);
    enableTenantFilter(tenantId);
  }

  /**
   * Enable Hibernate tenant filter for the current session.
   */
  private void enableTenantFilter(String tenantId) {
    Session session = entityManager.getEntityManager().unwrap(Session.class);
    session.enableFilter("tenantFilter")
        .setParameter("tenantId", tenantId);
  }

  /**
   * Disable Hibernate tenant filter for the current session.
   */
  private void disableTenantFilter() {
    Session session = entityManager.getEntityManager().unwrap(Session.class);
    session.disableFilter("tenantFilter");
  }

  /**
   * Flush and clear the entity manager to ensure changes are persisted
   * and the session is in a clean state.
   */
  protected void flushAndClear() {
    entityManager.flush();
    entityManager.clear();

    enableTenantFilter(TenantContext.getCurrentTenant());
  }
}
