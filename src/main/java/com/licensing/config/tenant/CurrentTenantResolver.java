package com.licensing.config.tenant;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Resolves the current tenant identifier for Hibernate multi-tenancy.
 */
@Component
public class CurrentTenantResolver implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

  private static final String DEFAULT_TENANT = "default";

  @Override
  public String resolveCurrentTenantIdentifier() {
    String tenantId = TenantContext.getCurrentTenant();
    return tenantId != null ? tenantId : DEFAULT_TENANT;
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }

  @Override
  public void customize(Map<String, Object> hibernateProperties) {
    hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
  }
}
