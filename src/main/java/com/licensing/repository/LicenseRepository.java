package com.licensing.repository;

import com.licensing.domain.License;
import com.licensing.domain.LicenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for License entities.
 * Provides tenant-aware data access methods.
 */
@Repository
public interface LicenseRepository extends JpaRepository<License, UUID> {

  /**
   * Find license by ID within the current tenant.
   * This method respects the tenant filter unlike the default findById.
   */
  @Query("SELECT l FROM License l WHERE l.id = :id")
  Optional<License> findByIdWithinTenant(@Param("id") UUID id);

  /**
   * Find licenses by organization ID within the current tenant.
   */
  List<License> findByOrganizationId(UUID organizationId);

  /**
   * Find licenses by customer email within the current tenant.
   */
  List<License> findByCustomerEmail(String customerEmail);

  /**
   * Find licenses by status within the current tenant.
   */
  List<License> findByStatus(LicenseStatus status);

  /**
   * Find licenses expiring before the specified date within the current tenant.
   */
  List<License> findByExpiryDateBefore(Instant date);

  /**
   * Find licenses by organization ID and status within the current tenant.
   */
  List<License> findByOrganizationIdAndStatus(UUID organizationId, LicenseStatus status);

  /**
   * Count licenses by organization ID within the current tenant.
   */
  long countByOrganizationId(UUID organizationId);

  /**
   * Count licenses by status within the current tenant.
   */
  long countByStatus(LicenseStatus status);

  /**
   * Find licenses with pagination within the current tenant.
   */
  @Override
  Page<License> findAll(Pageable pageable);
}
