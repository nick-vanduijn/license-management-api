package com.licensing.config.tenant;

/**
 * Thread-local tenant context for multi-tenant operations.
 * Stores the current tenant ID for the request thread.
 */
public final class TenantContext {

  private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

  private TenantContext() {

  }

  /**
   * Sets the current tenant ID for this thread.
   * 
   * @param tenantId the tenant ID to set
   * @throws IllegalArgumentException if tenantId is null, empty, or blank
   */
  public static void setCurrentTenant(String tenantId) {
    if (tenantId == null || tenantId.trim().isEmpty()) {
      throw new IllegalArgumentException("Tenant ID cannot be null or empty");
    }
    currentTenant.set(tenantId);
  }

  /**
   * Gets the current tenant ID for this thread.
   * 
   * @return the current tenant ID, or null if not set
   */
  public static String getCurrentTenant() {
    return currentTenant.get();
  }

  /**
   * Clears the current tenant context for this thread.
   */
  public static void clear() {
    currentTenant.remove();
  }
}
