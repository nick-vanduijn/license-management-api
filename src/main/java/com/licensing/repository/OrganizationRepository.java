package com.licensing.repository;

import com.licensing.domain.Organization;
import com.licensing.domain.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Organization entities.
 * Provides tenant-aware data access methods.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

  /**
   * Find organization by contact email within the current tenant.
   */
  Optional<Organization> findByContactEmail(String contactEmail);

  /**
   * Find all active organizations within the current tenant.
   */
  List<Organization> findByActiveTrue();

  /**
   * Find organizations by plan within the current tenant.
   */
  List<Organization> findByPlan(Plan plan);

  /**
   * Count active organizations within the current tenant.
   */
  long countByActiveTrue();

  /**
   * Check if organization exists by contact email within the current tenant.
   */
  boolean existsByContactEmail(String contactEmail);

  /**
   * Find organizations with pagination within the current tenant.
   */
  @Override
  Page<Organization> findAll(Pageable pageable);
}
