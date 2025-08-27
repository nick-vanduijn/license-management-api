package com.licensing.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Filter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Represents an organization within a tenant.
 * Organizations are the main entities that purchase and manage licenses.
 */
@Entity
@Table(name = "organizations")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Organization {

  private static final Pattern EMAIL_PATTERN = Pattern.compile(
      "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  @Id
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(name = "contact_email", nullable = false)
  private String contactEmail;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Plan plan;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  private Long version;

  protected Organization() {
  }

  public Organization(String name, String contactEmail, String tenantId, Plan plan) {
    validateInput(name, contactEmail, tenantId, plan);

    this.id = UUID.randomUUID();
    this.name = name;
    this.contactEmail = contactEmail;
    this.tenantId = tenantId;
    this.plan = plan;
    this.active = true;

    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void updateDetails(String name, String contactEmail, Plan plan) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Organization name cannot be null or empty");
    }
    if (contactEmail == null || contactEmail.trim().isEmpty()) {
      throw new IllegalArgumentException("Contact email cannot be null or empty");
    }
    if (!EMAIL_PATTERN.matcher(contactEmail).matches()) {
      throw new IllegalArgumentException("Contact email must be valid");
    }
    if (plan == null) {
      throw new IllegalArgumentException("Plan cannot be null");
    }

    this.name = name;
    this.contactEmail = contactEmail;
    this.plan = plan;
    this.updatedAt = Instant.now();
  }

  public void activate() {
    this.active = true;
    this.updatedAt = Instant.now();
  }

  public void deactivate() {
    this.active = false;
    this.updatedAt = Instant.now();
  }

  private void validateInput(String name, String contactEmail, String tenantId, Plan plan) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Organization name cannot be null or empty");
    }
    if (contactEmail == null || contactEmail.trim().isEmpty()) {
      throw new IllegalArgumentException("Contact email cannot be null or empty");
    }
    if (!EMAIL_PATTERN.matcher(contactEmail).matches()) {
      throw new IllegalArgumentException("Contact email must be valid");
    }
    if (tenantId == null || tenantId.trim().isEmpty()) {
      throw new IllegalArgumentException("Tenant ID cannot be null or empty");
    }
    if (plan == null) {
      throw new IllegalArgumentException("Plan cannot be null");
    }
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getContactEmail() {
    return contactEmail;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Plan getPlan() {
    return plan;
  }

  public boolean isActive() {
    return active;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Long getVersion() {
    return version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Organization that = (Organization) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Organization{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", contactEmail='" + contactEmail + '\'' +
        ", tenantId='" + tenantId + '\'' +
        ", plan=" + plan +
        ", active=" + active +
        '}';
  }
}
