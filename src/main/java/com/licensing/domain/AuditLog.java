package com.licensing.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an audit log entry for tracking changes to entities.
 */
@Entity
@Table(name = "audit_logs")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class AuditLog {

  @Id
  private UUID id;

  @Column(name = "entity_type", nullable = false)
  private String entityType;

  @Column(name = "entity_id", nullable = false)
  private UUID entityId;

  @Column(nullable = false)
  private String action;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Lob
  @Column(name = "details")
  private String details;

  @Column(nullable = false)
  private Instant timestamp;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  protected AuditLog() {
  }

  public AuditLog(String entityType, UUID entityId, String action, String userId,
      String tenantId, Map<String, Object> details) {
    validateInput(entityType, entityId, action, userId, tenantId);

    this.id = UUID.randomUUID();
    this.entityType = entityType;
    this.entityId = entityId;
    this.action = action;
    this.userId = userId;
    this.tenantId = tenantId;
    setDetails(details);
    this.timestamp = Instant.now();
  }

  private void setDetails(Map<String, Object> details) {
    if (details == null) {
      this.details = null;
      return;
    }
    try {
      this.details = objectMapper.writeValueAsString(details);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize details to JSON", e);
    }
  }

  private void validateInput(String entityType, UUID entityId, String action,
      String userId, String tenantId) {
    if (entityType == null || entityType.trim().isEmpty()) {
      throw new IllegalArgumentException("Entity type cannot be null or empty");
    }
    if (entityId == null) {
      throw new IllegalArgumentException("Entity ID cannot be null");
    }
    if (action == null || action.trim().isEmpty()) {
      throw new IllegalArgumentException("Action cannot be null or empty");
    }
    if (userId == null || userId.trim().isEmpty()) {
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }
    if (tenantId == null || tenantId.trim().isEmpty()) {
      throw new IllegalArgumentException("Tenant ID cannot be null or empty");
    }
  }

  public UUID getId() {
    return id;
  }

  public String getEntityType() {
    return entityType;
  }

  public UUID getEntityId() {
    return entityId;
  }

  public String getAction() {
    return action;
  }

  public String getUserId() {
    return userId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Map<String, Object> getDetails() {
    if (details == null) {
      return null;
    }
    try {
      TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
      };
      return objectMapper.readValue(details, typeRef);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to deserialize details from JSON", e);
    }
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    AuditLog auditLog = (AuditLog) o;
    return Objects.equals(id, auditLog.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "AuditLog{" +
        "id=" + id +
        ", entityType='" + entityType + '\'' +
        ", entityId=" + entityId +
        ", action='" + action + '\'' +
        ", userId='" + userId + '\'' +
        ", tenantId='" + tenantId + '\'' +
        ", timestamp=" + timestamp +
        '}';
  }
}
