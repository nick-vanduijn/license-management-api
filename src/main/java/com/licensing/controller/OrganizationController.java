package com.licensing.controller;

import com.licensing.service.OrganizationService;
import com.licensing.domain.Organization;
import com.licensing.domain.Plan;
import com.licensing.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import java.util.UUID;
import java.util.Optional;

/**
 * REST controller for organization management operations.
 * Provides endpoints for CRUD operations on organizations.
 */
@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organizations", description = "Organization management operations")
@SecurityRequirement(name = "TenantHeader")
public class OrganizationController {

  private final OrganizationService organizationService;

  public OrganizationController(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  @Operation(summary = "Create a new organization", description = "Creates a new organization with the specified details and assigns it to the current tenant")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Organization created successfully", content = @Content(schema = @Schema(implementation = Organization.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "Organization with this email already exists", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping
  public ResponseEntity<Organization> createOrganization(
      @Valid @RequestBody CreateOrganizationRequest request,
      @Parameter(description = "User ID performing the operation", required = true) @RequestHeader("X-User-ID") String userId) {

    try {
      Plan plan = Plan.valueOf(request.getPlan());
      Organization organization = organizationService.createOrganization(
          request.getName(),
          request.getContactEmail(),
          plan,
          userId);

      return new ResponseEntity<>(organization, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<Organization> getOrganization(@PathVariable UUID id) {
    Optional<Organization> organization = organizationService.findById(id);
    return organization.map(org -> ResponseEntity.ok(org))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<Page<Organization>> getAllOrganizations(Pageable pageable) {
    Page<Organization> organizations = organizationService.findAll(pageable);
    return ResponseEntity.ok(organizations);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Organization> updateOrganization(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateOrganizationRequest request,
      @RequestHeader("X-User-ID") String userId) {

    try {
      Plan plan = Plan.valueOf(request.getPlan());
      Organization organization = organizationService.updateOrganization(
          id,
          request.getName(),
          request.getContactEmail(),
          plan,
          userId);

      return ResponseEntity.ok(organization);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  @PatchMapping("/{id}/activate")
  public ResponseEntity<Organization> activateOrganization(
      @PathVariable UUID id,
      @RequestHeader("X-User-ID") String userId) {

    organizationService.activateOrganization(id, userId);
    Optional<Organization> organization = organizationService.findById(id);
    return organization.map(org -> ResponseEntity.ok(org))
        .orElse(ResponseEntity.notFound().build());
  }

  @PatchMapping("/{id}/deactivate")
  public ResponseEntity<Organization> deactivateOrganization(
      @PathVariable UUID id,
      @RequestHeader("X-User-ID") String userId) {

    organizationService.deactivateOrganization(id, userId);
    Optional<Organization> organization = organizationService.findById(id);
    return organization.map(org -> ResponseEntity.ok(org))
        .orElse(ResponseEntity.notFound().build());
  }

  public static class CreateOrganizationRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Contact email must be valid")
    private String contactEmail;

    @NotBlank(message = "Plan is required")
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
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Contact email must be valid")
    private String contactEmail;

    @NotBlank(message = "Plan is required")
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
