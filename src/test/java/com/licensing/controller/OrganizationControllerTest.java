package com.licensing.controller;

import com.licensing.service.OrganizationService;
import com.licensing.domain.Organization;
import com.licensing.domain.Plan;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(controllers = OrganizationController.class, excludeAutoConfiguration = { SecurityAutoConfiguration.class })
public class OrganizationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private OrganizationService organizationService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  public void shouldCreateOrganization() throws Exception {

    Organization organization = new Organization("Test Organization", "test@example.com", "tenant-1", Plan.ENTERPRISE);

    CreateOrganizationRequest request = new CreateOrganizationRequest();
    request.setName("Test Organization");
    request.setContactEmail("test@example.com");
    request.setPlan("ENTERPRISE");

    when(organizationService.createOrganization(
        "Test Organization",
        "test@example.com",
        Plan.ENTERPRISE,
        "test-user")).thenReturn(organization);

    mockMvc.perform(post("/api/v1/organizations")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .header("X-User-ID", "test-user"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Test Organization"))
        .andExpect(jsonPath("$.contactEmail").value("test@example.com"))
        .andExpect(jsonPath("$.plan").value("ENTERPRISE"))
        .andExpect(jsonPath("$.active").value(true));

    verify(organizationService).createOrganization("Test Organization", "test@example.com", Plan.ENTERPRISE,
        "test-user");
  }

  @Test
  public void shouldReturnBadRequestWhenCreatingOrganizationWithInvalidData() throws Exception {

    CreateOrganizationRequest request = new CreateOrganizationRequest();

    mockMvc.perform(post("/api/v1/organizations")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .header("X-User-ID", "test-user"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(organizationService);
  }

  @Test
  public void shouldGetOrganizationById() throws Exception {

    UUID organizationId = UUID.randomUUID();
    Organization organization = new Organization("Test Organization", "test@example.com", "tenant-1", Plan.ENTERPRISE);

    when(organizationService.findById(organizationId)).thenReturn(Optional.of(organization));

    mockMvc.perform(get("/api/v1/organizations/{id}", organizationId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Organization"))
        .andExpect(jsonPath("$.contactEmail").value("test@example.com"))
        .andExpect(jsonPath("$.plan").value("ENTERPRISE"))
        .andExpect(jsonPath("$.active").value(true));

    verify(organizationService).findById(organizationId);
  }

  @Test
  public void shouldReturnNotFoundWhenOrganizationDoesNotExist() throws Exception {

    UUID organizationId = UUID.randomUUID();
    when(organizationService.findById(organizationId))
        .thenReturn(Optional.empty());

    mockMvc.perform(get("/api/v1/organizations/{id}", organizationId))
        .andExpect(status().isNotFound());

    verify(organizationService).findById(organizationId);
  }

  @Test
  public void shouldGetAllOrganizations() throws Exception {

    Organization org1 = new Organization("Organization 1", "org1@example.com", "tenant-1", Plan.ENTERPRISE);
    Organization org2 = new Organization("Organization 2", "org2@example.com", "tenant-1", Plan.PROFESSIONAL);

    Pageable pageable = PageRequest.of(0, 10);
    Page<Organization> organizationsPage = new PageImpl<>(Arrays.asList(org1, org2), pageable, 2);
    when(organizationService.findAll(any(Pageable.class))).thenReturn(organizationsPage);

    mockMvc.perform(get("/api/v1/organizations"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].name").value("Organization 1"))
        .andExpect(jsonPath("$.content[1].name").value("Organization 2"));

    verify(organizationService).findAll(any(Pageable.class));
  }

  @Test
  public void shouldUpdateOrganization() throws Exception {

    UUID organizationId = UUID.randomUUID();
    Organization updatedOrganization = new Organization("Updated Organization", "updated@example.com", "tenant-1",
        Plan.ENTERPRISE);

    UpdateOrganizationRequest request = new UpdateOrganizationRequest();
    request.setName("Updated Organization");
    request.setContactEmail("updated@example.com");
    request.setPlan("ENTERPRISE");

    when(organizationService.updateOrganization(
        organizationId,
        "Updated Organization",
        "updated@example.com",
        Plan.ENTERPRISE,
        "test-user")).thenReturn(updatedOrganization);

    mockMvc.perform(put("/api/v1/organizations/{id}", organizationId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .header("X-User-ID", "test-user"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated Organization"))
        .andExpect(jsonPath("$.contactEmail").value("updated@example.com"))
        .andExpect(jsonPath("$.plan").value("ENTERPRISE"));

    verify(organizationService).updateOrganization(organizationId, "Updated Organization", "updated@example.com",
        Plan.ENTERPRISE, "test-user");
  }

  @Test
  public void shouldActivateOrganization() throws Exception {

    UUID organizationId = UUID.randomUUID();
    Organization activatedOrganization = new Organization("Test Organization", "test@example.com", "tenant-1",
        Plan.ENTERPRISE);

    when(organizationService.findById(organizationId)).thenReturn(Optional.of(activatedOrganization));
    doNothing().when(organizationService).activateOrganization(organizationId, "test-user");

    mockMvc.perform(patch("/api/v1/organizations/{id}/activate", organizationId)
        .header("X-User-ID", "test-user"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(true));

    verify(organizationService).activateOrganization(organizationId, "test-user");
  }

  @Test
  public void shouldDeactivateOrganization() throws Exception {

    UUID organizationId = UUID.randomUUID();
    Organization deactivatedOrganization = new Organization("Test Organization", "test@example.com", "tenant-1",
        Plan.ENTERPRISE);
    deactivatedOrganization.deactivate();

    when(organizationService.findById(organizationId)).thenReturn(Optional.of(deactivatedOrganization));
    doNothing().when(organizationService).deactivateOrganization(organizationId, "test-user");

    mockMvc.perform(patch("/api/v1/organizations/{id}/deactivate", organizationId)
        .header("X-User-ID", "test-user"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(false));

    verify(organizationService).deactivateOrganization(organizationId, "test-user");
  }

  public static class CreateOrganizationRequest {
    private String name;
    private String contactEmail;
    private String plan;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getContactEmail() {
      return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
      this.contactEmail = contactEmail;
    }

    public String getPlan() {
      return plan;
    }

    public void setPlan(String plan) {
      this.plan = plan;
    }
  }

  public static class UpdateOrganizationRequest {
    private String name;
    private String contactEmail;
    private String plan;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getContactEmail() {
      return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
      this.contactEmail = contactEmail;
    }

    public String getPlan() {
      return plan;
    }

    public void setPlan(String plan) {
      this.plan = plan;
    }
  }
}
