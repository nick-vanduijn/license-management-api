package com.licensing.repository;

import com.licensing.domain.License;
import com.licensing.domain.LicenseStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LicenseRepositoryTest extends BaseRepositoryTest {

  @Autowired
  private LicenseRepository licenseRepository;

  private final UUID organizationId = UUID.randomUUID();

  @Test
  void shouldSaveAndFindLicense() {
    License license = createValidLicense();

    License saved = licenseRepository.save(license);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getProductName()).isEqualTo("Test Product");
    assertThat(saved.getCustomerEmail()).isEqualTo("customer@example.com");
    assertThat(saved.getOrganizationId()).isEqualTo(organizationId);
    assertThat(saved.getStatus()).isEqualTo(LicenseStatus.ACTIVE);
  }

  @Test
  void shouldFindByIdWithinTenant() {
    License license = createValidLicense();
    License saved = licenseRepository.save(license);
    flushAndClear();
    entityManager.clear();

    Optional<License> found = licenseRepository.findById(saved.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getProductName()).isEqualTo("Test Product");
  }

  @Test
  void shouldFindByOrganizationId() {
    License license1 = createValidLicense();
    License license2 = createValidLicense();
    license2 = new License(UUID.randomUUID(), defaultTenantId, "Other Product", "other@example.com",
        Instant.now().plus(365, ChronoUnit.DAYS), Map.of());

    licenseRepository.save(license1);
    licenseRepository.save(license2);
    flushAndClear();

    List<License> organizationLicenses = licenseRepository.findByOrganizationId(organizationId);

    assertThat(organizationLicenses).hasSize(1);
    assertThat(organizationLicenses.get(0).getProductName()).isEqualTo("Test Product");
  }

  @Test
  void shouldFindByCustomerEmail() {
    String customerEmail = "unique-customer@example.com";
    License license = new License(organizationId, defaultTenantId, "Test Product", customerEmail,
        Instant.now().plus(365, ChronoUnit.DAYS), Map.of());
    licenseRepository.save(license);
    flushAndClear();

    List<License> customerLicenses = licenseRepository.findByCustomerEmail(customerEmail);

    assertThat(customerLicenses).hasSize(1);
    assertThat(customerLicenses.get(0).getCustomerEmail()).isEqualTo(customerEmail);
  }

  @Test
  void shouldFindByStatus() {
    License activeLicense = createValidLicense();
    License suspendedLicense = createValidLicense();
    suspendedLicense.suspend();

    licenseRepository.save(activeLicense);
    licenseRepository.save(suspendedLicense);
    flushAndClear();

    List<License> activeLicenses = licenseRepository.findByStatus(LicenseStatus.ACTIVE);
    List<License> suspendedLicenses = licenseRepository.findByStatus(LicenseStatus.SUSPENDED);

    assertThat(activeLicenses).hasSize(1);
    assertThat(suspendedLicenses).hasSize(1);
  }

  @Test
  void shouldFindExpiredLicenses() {
    Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);
    License expiredLicense = new License(organizationId, defaultTenantId, "Expired Product", "expired@example.com",
        pastDate, Map.of());
    License activeLicense = createValidLicense();

    licenseRepository.save(expiredLicense);
    licenseRepository.save(activeLicense);
    flushAndClear();

    List<License> expiredLicenses = licenseRepository.findByExpiryDateBefore(Instant.now());

    assertThat(expiredLicenses).hasSize(1);
    assertThat(expiredLicenses.get(0).getProductName()).isEqualTo("Expired Product");
  }

  @Test
  void shouldFindLicensesExpiringBefore() {
    Instant tomorrow = Instant.now().plus(1, ChronoUnit.DAYS);
    Instant nextWeek = Instant.now().plus(7, ChronoUnit.DAYS);

    License soonExpiringLicense = new License(organizationId, defaultTenantId, "Soon Expiring", "soon@example.com",
        tomorrow, Map.of());
    License laterExpiringLicense = new License(organizationId, defaultTenantId, "Later Expiring", "later@example.com",
        nextWeek, Map.of());

    licenseRepository.save(soonExpiringLicense);
    licenseRepository.save(laterExpiringLicense);
    flushAndClear();

    Instant threeDaysFromNow = Instant.now().plus(3, ChronoUnit.DAYS);
    List<License> expiringLicenses = licenseRepository.findByExpiryDateBefore(threeDaysFromNow);

    assertThat(expiringLicenses).hasSize(1);
    assertThat(expiringLicenses.get(0).getProductName()).isEqualTo("Soon Expiring");
  }

  @Test
  void shouldFindByOrganizationIdAndStatus() {
    License activeLicense = createValidLicense();
    License suspendedLicense = createValidLicense();
    suspendedLicense.suspend();

    licenseRepository.save(activeLicense);
    licenseRepository.save(suspendedLicense);
    flushAndClear();

    List<License> activeOrgLicenses = licenseRepository.findByOrganizationIdAndStatus(organizationId,
        LicenseStatus.ACTIVE);

    assertThat(activeOrgLicenses).hasSize(1);
    assertThat(activeOrgLicenses.get(0).getStatus()).isEqualTo(LicenseStatus.ACTIVE);
  }

  @Test
  void shouldPaginateResults() {
    for (int i = 0; i < 15; i++) {
      License license = new License(organizationId, defaultTenantId, "Product " + i, "customer" + i + "@example.com",
          Instant.now().plus(365, ChronoUnit.DAYS), Map.of());
      licenseRepository.save(license);
    }
    flushAndClear();

    Page<License> page = licenseRepository.findAll(PageRequest.of(0, 10));

    assertThat(page.getContent()).hasSize(10);
    assertThat(page.getTotalElements()).isEqualTo(15);
    assertThat(page.getTotalPages()).isEqualTo(2);
  }

  @Test
  void shouldCountByOrganizationId() {
    License license1 = createValidLicense();
    License license2 = createValidLicense();

    licenseRepository.save(license1);
    licenseRepository.save(license2);
    flushAndClear();

    long count = licenseRepository.countByOrganizationId(organizationId);

    assertThat(count).isEqualTo(2);
  }

  @Test
  void shouldCountByStatus() {
    License activeLicense = createValidLicense();
    License suspendedLicense = createValidLicense();
    suspendedLicense.suspend();

    licenseRepository.save(activeLicense);
    licenseRepository.save(suspendedLicense);
    flushAndClear();

    long activeCount = licenseRepository.countByStatus(LicenseStatus.ACTIVE);
    long suspendedCount = licenseRepository.countByStatus(LicenseStatus.SUSPENDED);

    assertThat(activeCount).isEqualTo(1);
    assertThat(suspendedCount).isEqualTo(1);
  }

  @Test
  public void shouldNotFindLicensesFromDifferentTenant() {

    License license = new License(organizationId, defaultTenantId, "Product A", "user@example.com",
        Instant.now().plus(30, ChronoUnit.DAYS), Map.of("feature1", true));
    licenseRepository.save(license);
    UUID savedId = license.getId();
    flushAndClear();

    switchToTenant("different-tenant");

    Optional<License> found = licenseRepository.findByIdWithinTenant(savedId);
    assertThat(found).isEmpty();
  }

  private License createValidLicense() {
    return new License(
        organizationId,
        defaultTenantId,
        "Test Product",
        "customer@example.com",
        Instant.now().plus(365, ChronoUnit.DAYS),
        Map.of("feature1", true, "maxUsers", 10));
  }
}
