package com.licensing.repository;

import com.licensing.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for AuditLog entities.
 * Provides tenant-aware data access methods for audit trails.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

  /**
   * Find audit logs by entity type and entity ID within the current tenant.
   */
  List<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId);

  /**
   * Find audit logs by entity type within the current tenant.
   */
  List<AuditLog> findByEntityType(String entityType);

  /**
   * Find audit logs by action within the current tenant.
   */
  List<AuditLog> findByAction(String action);

  /**
   * Find audit logs by user ID within the current tenant.
   */
  List<AuditLog> findByUserId(String userId);

  /**
   * Find audit logs between timestamps within the current tenant.
   */
  List<AuditLog> findByTimestampBetween(Instant startTime, Instant endTime);

  /**
   * Find audit logs after timestamp within the current tenant.
   */
  List<AuditLog> findByTimestampAfter(Instant timestamp);

  /**
   * Find audit logs before timestamp within the current tenant.
   */
  List<AuditLog> findByTimestampBefore(Instant timestamp);

  /**
   * Delete audit logs before timestamp within the current tenant.
   * Returns the number of deleted records.
   */
  @Modifying
  @Transactional
  long deleteByTimestampBefore(Instant timestamp);

  /**
   * Find audit logs with pagination within the current tenant.
   */
  @Override
  Page<AuditLog> findAll(Pageable pageable);
}
