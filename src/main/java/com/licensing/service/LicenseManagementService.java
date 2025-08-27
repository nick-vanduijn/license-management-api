package com.licensing.service;

import com.licensing.config.tenant.TenantContext;
import com.licensing.domain.AuditLog;
import com.licensing.domain.License;
import com.licensing.domain.LicenseStatus;
import com.licensing.domain.Organization;
import com.licensing.repository.AuditLogRepository;
import com.licensing.repository.LicenseRepository;
import com.licensing.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class LicenseManagementService {

  private final LicenseRepository licenseRepository;
  private final OrganizationRepository organizationRepository;
  private final AuditLogRepository auditLogRepository;
  private final LicenseSigningService licenseSigningService;

  public LicenseManagementService(LicenseRepository licenseRepository,
      OrganizationRepository organizationRepository,
      AuditLogRepository auditLogRepository,
      LicenseSigningService licenseSigningService) {
    this.licenseRepository = licenseRepository;
    this.organizationRepository = organizationRepository;
    this.auditLogRepository = auditLogRepository;
    this.licenseSigningService = licenseSigningService;
  }

  public License createLicense(UUID organizationId, String productName, String customerEmail,
      Instant expiryDate, Map<String, Object> features, String userId) {
    Organization organization = organizationRepository.findById(organizationId)
        .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

    if (!organization.isActive()) {
      throw new IllegalArgumentException("Cannot create license for inactive organization");
    }

    String tenantId = TenantContext.getCurrentTenant();
    License license = new License(organizationId, productName, customerEmail, tenantId, expiryDate, features);
    License savedLicense = licenseRepository.save(license);

    licenseSigningService.createSignedLicenseToken(savedLicense);

    createAuditLog("License", "CREATE", savedLicense.getId().toString(), userId, tenantId);

    return savedLicense;
  }

  @Transactional(readOnly = true)
  public Optional<License> findById(UUID id) {
    return licenseRepository.findById(id);
  }

  @Transactional(readOnly = true)
  public List<License> findByOrganizationId(UUID organizationId) {
    return licenseRepository.findByOrganizationId(organizationId);
  }

  @Transactional(readOnly = true)
  public List<License> findActiveLicenses() {
    return licenseRepository.findByStatus(LicenseStatus.ACTIVE);
  }

  @Transactional(readOnly = true)
  public List<License> findExpiredLicenses(Instant cutoffDate) {
    return licenseRepository.findByExpiryDateBefore(cutoffDate);
  }

  public License updateLicenseFeatures(UUID licenseId, Map<String, Object> features, String userId) {
    License license = licenseRepository.findById(licenseId)
        .orElseThrow(() -> new IllegalArgumentException("License not found"));

    license.updateFeatures(features);
    License savedLicense = licenseRepository.save(license);

    licenseSigningService.createSignedLicenseToken(savedLicense);

    String tenantId = TenantContext.getCurrentTenant();
    createAuditLog("License", "UPDATE", savedLicense.getId().toString(), userId, tenantId);

    return savedLicense;
  }

  public License extendLicense(UUID licenseId, Instant newExpiryDate, String userId) {
    License license = licenseRepository.findById(licenseId)
        .orElseThrow(() -> new IllegalArgumentException("License not found"));

    license.extend(newExpiryDate);
    License savedLicense = licenseRepository.save(license);

    licenseSigningService.createSignedLicenseToken(savedLicense);

    String tenantId = TenantContext.getCurrentTenant();
    createAuditLog("License", "EXTEND", savedLicense.getId().toString(), userId, tenantId);

    return savedLicense;
  }

  public void suspendLicense(UUID licenseId, String userId) {
    License license = licenseRepository.findById(licenseId)
        .orElseThrow(() -> new IllegalArgumentException("License not found"));

    license.suspend();
    licenseRepository.save(license);

    String tenantId = TenantContext.getCurrentTenant();
    createAuditLog("License", "SUSPEND", license.getId().toString(), userId, tenantId);
  }

  public void reactivateLicense(UUID licenseId, String userId) {
    License license = licenseRepository.findById(licenseId)
        .orElseThrow(() -> new IllegalArgumentException("License not found"));

    license.activate();
    licenseRepository.save(license);

    licenseSigningService.createSignedLicenseToken(license);

    String tenantId = TenantContext.getCurrentTenant();
    createAuditLog("License", "REACTIVATE", license.getId().toString(), userId, tenantId);
  }

  public void revokeLicense(UUID licenseId, String userId) {
    License license = licenseRepository.findById(licenseId)
        .orElseThrow(() -> new IllegalArgumentException("License not found"));

    license.revoke();
    licenseRepository.save(license);

    String tenantId = TenantContext.getCurrentTenant();
    createAuditLog("License", "REVOKE", license.getId().toString(), userId, tenantId);
  }

  @Transactional(readOnly = true)
  public boolean validateLicense(UUID licenseId, String signedToken) {
    Optional<License> licenseOpt = licenseRepository.findById(licenseId);
    if (licenseOpt.isEmpty()) {
      return false;
    }

    License license = licenseOpt.get();
    return licenseSigningService.verifySignature(license, signedToken);
  }

  @Transactional(readOnly = true)
  public List<License> findByStatus(LicenseStatus status) {
    return licenseRepository.findByStatus(status);
  }

  @Transactional(readOnly = true)
  public long countByStatus(LicenseStatus status) {
    return licenseRepository.countByStatus(status);
  }

  @Transactional(readOnly = true)
  public String getSignedLicenseToken(UUID licenseId) {
    License license = licenseRepository.findById(licenseId)
        .orElseThrow(() -> new IllegalArgumentException("License not found"));

    return licenseSigningService.createSignedLicenseToken(license);
  }

  private void createAuditLog(String entityType, String action, String entityId, String userId, String tenantId) {
    AuditLog auditLog = new AuditLog(entityType, UUID.fromString(entityId), action, userId, tenantId, null);
    auditLogRepository.save(auditLog);
  }
}
