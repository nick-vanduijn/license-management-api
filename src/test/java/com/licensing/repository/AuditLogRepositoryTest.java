package com.licensing.repository;

import com.licensing.domain.AuditLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogRepositoryTest extends BaseRepositoryTest {

  @Autowired
  private AuditLogRepository auditLogRepository;

  @Test
  void shouldSaveAndFindAuditLog() {
    AuditLog auditLog = createValidAuditLog();

    AuditLog saved = auditLogRepository.save(auditLog);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getEntityType()).isEqualTo("Organization");
    assertThat(saved.getAction()).isEqualTo("CREATE");
    assertThat(saved.getUserId()).isEqualTo("user-123");
    assertThat(saved.getTenantId()).isEqualTo(defaultTenantId);
  }

  @Test
  void shouldFindByEntityTypeAndEntityId() {
    UUID entityId = UUID.randomUUID();
    AuditLog auditLog = new AuditLog("Organization", entityId, "CREATE",
        "user-123", defaultTenantId, Map.of("name", "Test Corp"));
    auditLogRepository.save(auditLog);
    flushAndClear();

    List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId("Organization", entityId);

    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getEntityId()).isEqualTo(entityId);
  }

  @Test
  void shouldFindByEntityType() {
    AuditLog orgLog = new AuditLog("Organization", UUID.randomUUID(), "CREATE",
        "user-123", defaultTenantId, Map.of());
    AuditLog licenseLog = new AuditLog("License", UUID.randomUUID(), "CREATE",
        "user-123", defaultTenantId, Map.of());

    auditLogRepository.save(orgLog);
    auditLogRepository.save(licenseLog);
    flushAndClear();

    List<AuditLog> orgLogs = auditLogRepository.findByEntityType("Organization");

    assertThat(orgLogs).hasSize(1);
    assertThat(orgLogs.get(0).getEntityType()).isEqualTo("Organization");
  }

  @Test
  void shouldFindByAction() {
    AuditLog createLog = new AuditLog("Organization", UUID.randomUUID(), "CREATE",
        "user-123", defaultTenantId, Map.of());
    AuditLog updateLog = new AuditLog("Organization", UUID.randomUUID(), "UPDATE",
        "user-123", defaultTenantId, Map.of());

    auditLogRepository.save(createLog);
    auditLogRepository.save(updateLog);
    flushAndClear();

    List<AuditLog> createLogs = auditLogRepository.findByAction("CREATE");

    assertThat(createLogs).hasSize(1);
    assertThat(createLogs.get(0).getAction()).isEqualTo("CREATE");
  }

  @Test
  void shouldFindByUserId() {
    AuditLog user1Log = new AuditLog("Organization", UUID.randomUUID(), "CREATE",
        "user-123", defaultTenantId, Map.of());
    AuditLog user2Log = new AuditLog("Organization", UUID.randomUUID(), "CREATE",
        "user-456", defaultTenantId, Map.of());

    auditLogRepository.save(user1Log);
    auditLogRepository.save(user2Log);
    flushAndClear();

    List<AuditLog> user1Logs = auditLogRepository.findByUserId("user-123");

    assertThat(user1Logs).hasSize(1);
    assertThat(user1Logs.get(0).getUserId()).isEqualTo("user-123");
  }

  @Test
  void shouldFindByTimestampBetween() {
    Instant startTime = Instant.now().minus(1, ChronoUnit.HOURS);
    Instant endTime = Instant.now().plus(1, ChronoUnit.HOURS);
    Instant pastTime = Instant.now().minus(2, ChronoUnit.HOURS);

    AuditLog recentLog = createValidAuditLog();
    AuditLog oldLog = new AuditLog("Organization", UUID.randomUUID(), "CREATE",
        "user-123", defaultTenantId, Map.of());

    auditLogRepository.save(recentLog);
    auditLogRepository.save(oldLog);

    setTimestamp(oldLog, pastTime);
    auditLogRepository.save(oldLog);
    flushAndClear();

    List<AuditLog> recentLogs = auditLogRepository.findByTimestampBetween(startTime, endTime);

    assertThat(recentLogs).hasSize(1);
  }

  @Test
  void shouldFindByTimestampAfter() {
    Instant cutoffTime = Instant.now().minus(30, ChronoUnit.MINUTES);

    AuditLog recentLog = createValidAuditLog();
    auditLogRepository.save(recentLog);
    flushAndClear();

    List<AuditLog> recentLogs = auditLogRepository.findByTimestampAfter(cutoffTime);

    assertThat(recentLogs).hasSize(1);
  }

  @Test
  void shouldFindByTimestampBefore() {
    Instant cutoffTime = Instant.now().plus(1, ChronoUnit.HOURS);

    AuditLog log = createValidAuditLog();
    auditLogRepository.save(log);
    flushAndClear();

    List<AuditLog> oldLogs = auditLogRepository.findByTimestampBefore(cutoffTime);

    assertThat(oldLogs).hasSize(1);
  }

  @Test
  void shouldPaginateResults() {
    for (int i = 0; i < 15; i++) {
      AuditLog log = new AuditLog("Organization", UUID.randomUUID(), "CREATE",
          "user-" + i, defaultTenantId, Map.of());
      auditLogRepository.save(log);
    }
    flushAndClear();

    Page<AuditLog> page = auditLogRepository.findAll(PageRequest.of(0, 10));

    assertThat(page.getContent()).hasSize(10);
    assertThat(page.getTotalElements()).isEqualTo(15);
    assertThat(page.getTotalPages()).isEqualTo(2);
  }

  @Test
  void shouldDeleteByTimestampBefore() {
    Instant cutoffTime = Instant.now().minus(30, ChronoUnit.DAYS);
    Instant pastTime = Instant.now().minus(60, ChronoUnit.DAYS);

    AuditLog recentLog = createValidAuditLog();
    AuditLog oldLog = new AuditLog("Organization", UUID.randomUUID(), "CREATE",
        "user-123", defaultTenantId, Map.of());

    auditLogRepository.save(recentLog);
    auditLogRepository.save(oldLog);

    setTimestamp(oldLog, pastTime);
    auditLogRepository.save(oldLog);
    flushAndClear();

    long deletedCount = auditLogRepository.deleteByTimestampBefore(cutoffTime);

    assertThat(deletedCount).isEqualTo(1);

    List<AuditLog> remainingLogs = auditLogRepository.findAll();
    assertThat(remainingLogs).hasSize(1);
    assertThat(remainingLogs.get(0).getId()).isEqualTo(recentLog.getId());
  }

  @Test
  void shouldNotFindLogsFromDifferentTenant() {
    AuditLog log = createValidAuditLog();
    auditLogRepository.save(log);
    flushAndClear();

    switchToTenant("different-tenant");

    List<AuditLog> logs = auditLogRepository.findByUserId("user-123");

    assertThat(logs).isEmpty();
  }

  private AuditLog createValidAuditLog() {
    return new AuditLog(
        "Organization",
        UUID.randomUUID(),
        "CREATE",
        "user-123",
        defaultTenantId,
        Map.of("name", "Test Corp", "plan", "BASIC"));
  }

  private void setTimestamp(AuditLog auditLog, Instant timestamp) {
    try {
      var field = AuditLog.class.getDeclaredField("timestamp");
      field.setAccessible(true);
      field.set(auditLog, timestamp);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
