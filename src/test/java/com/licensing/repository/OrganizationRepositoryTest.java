package com.licensing.repository;

import com.licensing.domain.Organization;
import com.licensing.domain.Plan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationRepositoryTest extends BaseRepositoryTest {

  @Autowired
  private OrganizationRepository organizationRepository;

  @Test
  void shouldSaveAndFindOrganization() {
    Organization org = new Organization("Test Corp", "test@example.com", defaultTenantId, Plan.BASIC);

    Organization saved = organizationRepository.save(org);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getName()).isEqualTo("Test Corp");
    assertThat(saved.getContactEmail()).isEqualTo("test@example.com");
    assertThat(saved.getTenantId()).isEqualTo(defaultTenantId);
    assertThat(saved.getPlan()).isEqualTo(Plan.BASIC);
  }

  @Test
  void shouldFindByIdWithinTenant() {
    Organization org = new Organization("Test Corp", "test@example.com", defaultTenantId, Plan.BASIC);
    Organization saved = organizationRepository.save(org);
    flushAndClear();

    Optional<Organization> found = organizationRepository.findById(saved.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Test Corp");
  }

  @Test
  void shouldFindByContactEmail() {
    String email = "unique@example.com";
    Organization org = new Organization("Test Corp", email, defaultTenantId, Plan.BASIC);
    organizationRepository.save(org);
    flushAndClear();

    Optional<Organization> found = organizationRepository.findByContactEmail(email);

    assertThat(found).isPresent();
    assertThat(found.get().getContactEmail()).isEqualTo(email);
  }

  @Test
  void shouldNotFindByContactEmailFromDifferentTenant() {
    String email = "isolated@example.com";
    Organization org = new Organization("Test Corp", email, "other-tenant", Plan.BASIC);
    organizationRepository.save(org);
    flushAndClear();

    switchToTenant("different-tenant");

    Optional<Organization> found = organizationRepository.findByContactEmail(email);

    assertThat(found).isEmpty();
  }

  @Test
  void shouldFindActiveOrganizations() {
    Organization activeOrg = new Organization("Active Corp", "active@example.com", defaultTenantId, Plan.BASIC);
    Organization inactiveOrg = new Organization("Inactive Corp", "inactive@example.com", defaultTenantId, Plan.BASIC);
    inactiveOrg.deactivate();

    organizationRepository.save(activeOrg);
    organizationRepository.save(inactiveOrg);
    flushAndClear();

    List<Organization> activeOrgs = organizationRepository.findByActiveTrue();

    assertThat(activeOrgs).hasSize(1);
    assertThat(activeOrgs.get(0).getName()).isEqualTo("Active Corp");
  }

  @Test
  void shouldFindByPlan() {
    Organization basicOrg = new Organization("Basic Corp", "basic@example.com", defaultTenantId, Plan.BASIC);
    Organization enterpriseOrg = new Organization("Enterprise Corp", "enterprise@example.com", defaultTenantId,
        Plan.ENTERPRISE);

    organizationRepository.save(basicOrg);
    organizationRepository.save(enterpriseOrg);
    flushAndClear();

    List<Organization> basicOrgs = organizationRepository.findByPlan(Plan.BASIC);

    assertThat(basicOrgs).hasSize(1);
    assertThat(basicOrgs.get(0).getName()).isEqualTo("Basic Corp");
  }

  @Test
  void shouldPaginateResults() {
    for (int i = 0; i < 15; i++) {
      Organization org = new Organization("Corp " + i, "corp" + i + "@example.com", defaultTenantId, Plan.BASIC);
      organizationRepository.save(org);
    }
    flushAndClear();

    Page<Organization> page = organizationRepository.findAll(PageRequest.of(0, 10));

    assertThat(page.getContent()).hasSize(10);
    assertThat(page.getTotalElements()).isEqualTo(15);
    assertThat(page.getTotalPages()).isEqualTo(2);
  }

  @Test
  void shouldCountActiveOrganizations() {
    Organization activeOrg1 = new Organization("Active Corp 1", "active1@example.com", defaultTenantId, Plan.BASIC);
    Organization activeOrg2 = new Organization("Active Corp 2", "active2@example.com", defaultTenantId, Plan.BASIC);
    Organization inactiveOrg = new Organization("Inactive Corp", "inactive@example.com", defaultTenantId, Plan.BASIC);
    inactiveOrg.deactivate();

    organizationRepository.save(activeOrg1);
    organizationRepository.save(activeOrg2);
    organizationRepository.save(inactiveOrg);
    flushAndClear();

    long activeCount = organizationRepository.countByActiveTrue();

    assertThat(activeCount).isEqualTo(2);
  }

  @Test
  void shouldExistsByContactEmail() {
    String email = "exists@example.com";
    Organization org = new Organization("Test Corp", email, defaultTenantId, Plan.BASIC);
    organizationRepository.save(org);
    flushAndClear();

    boolean exists = organizationRepository.existsByContactEmail(email);

    assertThat(exists).isTrue();
  }

  @Test
  void shouldNotExistsByContactEmailForDifferentTenant() {
    String email = "isolated-exists@example.com";
    Organization org = new Organization("Test Corp", email, "other-tenant", Plan.BASIC);
    organizationRepository.save(org);
    flushAndClear();

    switchToTenant("different-tenant");

    boolean exists = organizationRepository.existsByContactEmail(email);

    assertThat(exists).isFalse();
  }
}
