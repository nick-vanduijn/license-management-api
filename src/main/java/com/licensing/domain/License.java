package com.licensing.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Represents a software license issued to a customer within an organization.
 */
@Entity
@Table(name = "licenses")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class License {

  private static final Pattern EMAIL_PATTERN = Pattern.compile(
      "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  @Id
  private UUID id;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "product_name", nullable = false)
  private String productName;

  @Column(name = "customer_email", nullable = false)
  private String customerEmail;

  @Column(name = "expiry_date", nullable = false)
  private Instant expiryDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LicenseStatus status = LicenseStatus.ACTIVE;

  @Lob
  @Column(name = "features")
  private String featuresJson;

  @Transient
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Column(name = "signature")
  private String signature;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  private Long version;

  public License() {
  }

  public License(UUID organizationId, String tenantId, String productName, String customerEmail,
      Instant expiryDate, Map<String, Object> features) {
    validateInput(organizationId, tenantId, productName, customerEmail, expiryDate, features);

    this.id = UUID.randomUUID();
    this.organizationId = organizationId;
    this.tenantId = tenantId;
    this.productName = productName;
    this.customerEmail = customerEmail;
    this.expiryDate = expiryDate;
    setFeatures(features);
    this.status = LicenseStatus.ACTIVE;

    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;

    if (isExpired()) {
      this.status = LicenseStatus.EXPIRED;
    }
  }

  public void activate() {
    this.status = LicenseStatus.ACTIVE;
    this.updatedAt = Instant.now();
  }

  public void suspend() {
    this.status = LicenseStatus.SUSPENDED;
    this.updatedAt = Instant.now();
  }

  public void revoke() {
    this.status = LicenseStatus.REVOKED;
    this.updatedAt = Instant.now();
  }

  public void expire() {
    this.status = LicenseStatus.EXPIRED;
    this.updatedAt = Instant.now();
  }

  public void extend(Instant newExpiryDate) {
    if (newExpiryDate == null) {
      throw new IllegalArgumentException("New expiry date cannot be null");
    }
    if (newExpiryDate.isBefore(this.expiryDate) || newExpiryDate.equals(this.expiryDate)) {
      throw new IllegalArgumentException("New expiry date must be after current expiry date");
    }

    this.expiryDate = newExpiryDate;
    this.updatedAt = Instant.now();
  }

  public void updateFeatures(Map<String, Object> newFeatures) {
    if (newFeatures == null) {
      throw new IllegalArgumentException("Features cannot be null");
    }

    setFeatures(newFeatures);
    this.updatedAt = Instant.now();
  }

  public void setSignature(String signature) {
    this.signature = signature;
    this.updatedAt = Instant.now();
  }

  public boolean isActive() {
    return status == LicenseStatus.ACTIVE && !isExpired();
  }

  public boolean isExpired() {
    return Instant.now().isAfter(expiryDate);
  }

  private void validateInput(UUID organizationId, String tenantId, String productName, String customerEmail,
      Instant expiryDate, Map<String, Object> features) {
    if (organizationId == null) {
      throw new IllegalArgumentException("Organization ID cannot be null");
    }
    if (tenantId == null || tenantId.trim().isEmpty()) {
      throw new IllegalArgumentException("Tenant ID cannot be null or empty");
    }
    if (productName == null || productName.trim().isEmpty()) {
      throw new IllegalArgumentException("Product name cannot be null or empty");
    }
    if (customerEmail == null || customerEmail.trim().isEmpty()) {
      throw new IllegalArgumentException("Customer email cannot be null or empty");
    }
    if (!EMAIL_PATTERN.matcher(customerEmail).matches()) {
      throw new IllegalArgumentException("Customer email must be valid");
    }
    if (expiryDate == null) {
      throw new IllegalArgumentException("Expiry date cannot be null");
    }
    if (features == null) {
      throw new IllegalArgumentException("Features cannot be null");
    }
  }

  public Map<String, Object> getFeatures() {
    if (featuresJson == null || featuresJson.trim().isEmpty()) {
      return new HashMap<>();
    }
    try {
      return objectMapper.readValue(featuresJson, new TypeReference<Map<String, Object>>() {
      });
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to deserialize features", e);
    }
  }

  public void setFeatures(Map<String, Object> features) {
    if (features == null) {
      this.featuresJson = null;
      return;
    }
    try {
      this.featuresJson = objectMapper.writeValueAsString(features);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize features", e);
    }
  }

  public UUID getId() {
    return id;
  }

  public UUID getOrganizationId() {
    return organizationId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getProductName() {
    return productName;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public Instant getExpiryDate() {
    return expiryDate;
  }

  public LicenseStatus getStatus() {
    return status;
  }

  public String getSignature() {
    return signature;
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
    License license = (License) o;
    return Objects.equals(id, license.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "License{" +
        "id=" + id +
        ", organizationId=" + organizationId +
        ", productName='" + productName + '\'' +
        ", customerEmail='" + customerEmail + '\'' +
        ", status=" + status +
        ", expiryDate=" + expiryDate +
        '}';
  }
}
