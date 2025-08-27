package com.licensing.domain;

/**
 * Represents the status of a license throughout its lifecycle.
 */
public enum LicenseStatus {
  ACTIVE("Active"),
  SUSPENDED("Suspended"),
  REVOKED("Revoked"),
  EXPIRED("Expired");

  private final String displayName;

  LicenseStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public boolean isActive() {
    return this == ACTIVE;
  }
}
