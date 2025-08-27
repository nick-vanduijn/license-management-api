package com.licensing.service;

import com.licensing.config.tenant.TenantContext;
import com.licensing.domain.AuditLog;
import com.licensing.domain.Organization;
import com.licensing.domain.Plan;
import com.licensing.repository.AuditLogRepository;
import com.licensing.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final AuditLogRepository auditLogRepository;

  public OrganizationService(OrganizationRepository organizationRepository, AuditLogRepository auditLogRepository) {
    this.organizationRepository = organizationRepository;
    this.auditLogRepository = auditLogRepository;
  }

  public Organization createOrganization(String name, String contactEmail, Plan plan, String userId) {
    if (organizationRepository.existsByContactEmail(contactEmail)) {
      throw new IllegalArgumentException("Organization with email " + contactEmail + " already exists");
    }

    String tenantId = TenantContext.getCurrentTenant();
    Organization organization = new Organization(name, contactEmail, tenantId, plan);
    Organization savedOrganization = organizationRepository.save(organization);

    createAuditLog("Organization", "CREATE", savedOrganization.getId().toString(), userId, tenantId);

    return savedOrganization;
  }

  @Transactional(readOnly = true)
  public Optional<Organization> findById(UUID id) {
    return organizationRepository.findById(id);
  }

  @Transactional(readOnly = true)
  public Optional<Organization> findByContactEmail(String contactEmail) {
    return organizationRepository.findByContactEmail(contactEmail);
  }

  public Organization updateOrganization(UUID id, String name, String contactEmail, Plan plan, String userId) {
    Organization organization = organizationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

    if (!organization.getContactEmail().equals(contactEmail) &&
        organizationRepository.existsByContactEmail(contactEmail)) {
      throw new IllegalArgumentException("Organization with email " + contactEmail + " already exists");
    }

    organization.updateDetails(name, contactEmail, plan);
    Organization savedOrganization = organizationRepository.save(organization);

    String tenantId = TenantContext.getCurrentTenant();
    createAuditLog("Organization", "UPDATE", savedOrganization.getId().toString(), userId, tenantId);

    return savedOrganization;
  }

  public void deactivateOrganization(UUID id, String userId) {
    Organization organization = organizationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

    organization.deactivate();
    organizationRepository.save(organization);

    String tenantId = TenantContext.getCurrentTenant();
    createAuditLog("Organization", "DEACTIVATE", organization.getId().toString(), userId, tenantId);
  }

  public void activateOrganization(UUID id, String userId) {
    Organization organization = organizationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

    organization.activate();
    organizationRepository.save(organization);

    String tenantId = TenantContext.getCurrentTenant();
    createAuditLog("Organization", "ACTIVATE", organization.getId().toString(), userId, tenantId);
  }

  @Transactional(readOnly = true)
  public List<Organization> findActiveOrganizations() {
    return organizationRepository.findByActiveTrue();
  }

  @Transactional(readOnly = true)
  public List<Organization> findByPlan(Plan plan) {
    return organizationRepository.findByPlan(plan);
  }

  @Transactional(readOnly = true)
  public Page<Organization> findAll(Pageable pageable) {
    return organizationRepository.findAll(pageable);
  }

  @Transactional(readOnly = true)
  public long countActiveOrganizations() {
    return organizationRepository.countByActiveTrue();
  }

  private void createAuditLog(String entityType, String action, String entityId, String userId, String tenantId) {
    AuditLog auditLog = new AuditLog(entityType, UUID.fromString(entityId), action, userId, tenantId, null);
    auditLogRepository.save(auditLog);
  }
}
