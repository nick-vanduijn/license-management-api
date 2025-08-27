package com.licensing.domain;

/**
 * Represents different subscription plans available for organizations.
 */
public enum Plan {
  BASIC("Basic Plan", 100, 1),
  PROFESSIONAL("Professional Plan", 1000, 5),
  ENTERPRISE("Enterprise Plan", 10000, 25);

  private final String displayName;
  private final int maxLicenses;
  private final int maxUsers;

  Plan(String displayName, int maxLicenses, int maxUsers) {
    this.displayName = displayName;
    this.maxLicenses = maxLicenses;
    this.maxUsers = maxUsers;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getMaxLicenses() {
    return maxLicenses;
  }

  public int getMaxUsers() {
    return maxUsers;
  }
}
