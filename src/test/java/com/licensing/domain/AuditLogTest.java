package com.licensing.domain;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditLogTest {

  @Test
  void shouldCreateAuditLogWithRequiredFields() {
    String entityType = "Organization";
    UUID entityId = UUID.randomUUID();
    String action = "CREATE";
    String userId = "user-123";
    String tenantId = "tenant-456";
    Map<String, Object> details = Map.of("name", "Acme Corp", "plan", "ENTERPRISE");

    AuditLog auditLog = new AuditLog(entityType, entityId, action, userId, tenantId, details);

    assertThat(auditLog.getEntityType()).isEqualTo(entityType);
    assertThat(auditLog.getEntityId()).isEqualTo(entityId);
    assertThat(auditLog.getAction()).isEqualTo(action);
    assertThat(auditLog.getUserId()).isEqualTo(userId);
    assertThat(auditLog.getTenantId()).isEqualTo(tenantId);
    assertThat(auditLog.getDetails()).isEqualTo(details);
    assertThat(auditLog.getId()).isNotNull();
    assertThat(auditLog.getTimestamp()).isNotNull();
  }

  @Test
  void shouldThrowExceptionWhenEntityTypeIsNull() {
    assertThatThrownBy(() -> new AuditLog(null, UUID.randomUUID(), "CREATE",
        "user-123", "tenant-456", Map.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Entity type cannot be null or empty");
  }

  @Test
  void shouldThrowExceptionWhenEntityTypeIsEmpty() {
    assertThatThrownBy(() -> new AuditLog("", UUID.randomUUID(), "CREATE",
        "user-123", "tenant-456", Map.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Entity type cannot be null or empty");
  }

  @Test
  void shouldThrowExceptionWhenEntityIdIsNull() {
    assertThatThrownBy(() -> new AuditLog("Organization", null, "CREATE",
        "user-123", "tenant-456", Map.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Entity ID cannot be null");
  }

  @Test
  void shouldThrowExceptionWhenActionIsNull() {
    assertThatThrownBy(() -> new AuditLog("Organization", UUID.randomUUID(), null,
        "user-123", "tenant-456", Map.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Action cannot be null or empty");
  }

  @Test
  void shouldThrowExceptionWhenActionIsEmpty() {
    assertThatThrownBy(() -> new AuditLog("Organization", UUID.randomUUID(), "",
        "user-123", "tenant-456", Map.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Action cannot be null or empty");
  }

  @Test
  void shouldThrowExceptionWhenUserIdIsNull() {
    assertThatThrownBy(() -> new AuditLog("Organization", UUID.randomUUID(), "CREATE",
        null, "tenant-456", Map.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User ID cannot be null or empty");
  }

  @Test
  void shouldThrowExceptionWhenTenantIdIsNull() {
    assertThatThrownBy(() -> new AuditLog("Organization", UUID.randomUUID(), "CREATE",
        "user-123", null, Map.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Tenant ID cannot be null or empty");
  }

  @Test
  void shouldAllowNullDetails() {
    AuditLog auditLog = new AuditLog("Organization", UUID.randomUUID(), "CREATE",
        "user-123", "tenant-456", null);

    assertThat(auditLog.getDetails()).isNull();
  }

  @Test
  void shouldHaveToStringRepresentation() {
    AuditLog auditLog = new AuditLog("Organization", UUID.randomUUID(), "CREATE",
        "user-123", "tenant-456", Map.of("test", "value"));

    String toString = auditLog.toString();

    assertThat(toString).contains("AuditLog{");
    assertThat(toString).contains("entityType='Organization'");
    assertThat(toString).contains("action='CREATE'");
    assertThat(toString).contains("userId='user-123'");
    assertThat(toString).contains("tenantId='tenant-456'");
  }

  @Test
  void shouldImplementEqualsAndHashCodeBasedOnId() {
    UUID id = UUID.randomUUID();
    AuditLog auditLog1 = new AuditLog("Organization", UUID.randomUUID(), "CREATE",
        "user-123", "tenant-456", Map.of());
    AuditLog auditLog2 = new AuditLog("License", UUID.randomUUID(), "UPDATE",
        "user-789", "tenant-123", Map.of());

    setId(auditLog1, id);
    setId(auditLog2, id);

    assertThat(auditLog1).isEqualTo(auditLog2);
    assertThat(auditLog1.hashCode()).isEqualTo(auditLog2.hashCode());
  }

  private void setId(AuditLog auditLog, UUID id) {
    try {
      var field = AuditLog.class.getDeclaredField("id");
      field.setAccessible(true);
      field.set(auditLog, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
