package com.licensing.controller;

import com.licensing.service.LicenseManagementService;
import com.licensing.domain.License;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/licenses")
public class LicenseController {

  private final LicenseManagementService licenseManagementService;

  public LicenseController(LicenseManagementService licenseManagementService) {
    this.licenseManagementService = licenseManagementService;
  }

  @PostMapping
  public ResponseEntity<License> createLicense(
      @Valid @RequestBody CreateLicenseRequest request,
      @RequestHeader("X-User-ID") String userId) {

    License license = licenseManagementService.createLicense(
        request.getOrganizationId(),
        request.getProductName(),
        request.getCustomerEmail(),
        request.getExpiresAt(),
        request.getFeatures(),
        userId);

    return ResponseEntity.status(HttpStatus.CREATED).body(license);
  }

  @GetMapping("/{id}")
  public ResponseEntity<License> getLicenseById(@PathVariable UUID id) {
    Optional<License> license = licenseManagementService.findById(id);
    return license.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/organization/{organizationId}")
  public ResponseEntity<List<License>> getLicensesByOrganization(@PathVariable UUID organizationId) {
    List<License> licenses = licenseManagementService.findByOrganizationId(organizationId);
    return ResponseEntity.ok(licenses);
  }

  @PatchMapping("/{id}/suspend")
  public ResponseEntity<Void> suspendLicense(
      @PathVariable UUID id,
      @RequestHeader("X-User-ID") String userId) {

    licenseManagementService.suspendLicense(id, userId);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{id}/reactivate")
  public ResponseEntity<Void> reactivateLicense(
      @PathVariable UUID id,
      @RequestHeader("X-User-ID") String userId) {

    licenseManagementService.reactivateLicense(id, userId);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{id}/revoke")
  public ResponseEntity<Void> revokeLicense(
      @PathVariable UUID id,
      @RequestHeader("X-User-ID") String userId) {

    licenseManagementService.revokeLicense(id, userId);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{id}/extend")
  public ResponseEntity<License> extendLicense(
      @PathVariable UUID id,
      @Valid @RequestBody ExtendLicenseRequest request,
      @RequestHeader("X-User-ID") String userId) {

    License extendedLicense = licenseManagementService.extendLicense(
        id,
        request.getNewExpiryDate(),
        userId);

    return ResponseEntity.ok(extendedLicense);
  }

  @PatchMapping("/{id}/features")
  public ResponseEntity<License> updateLicenseFeatures(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateLicenseFeaturesRequest request,
      @RequestHeader("X-User-ID") String userId) {

    License updatedLicense = licenseManagementService.updateLicenseFeatures(
        id,
        request.getFeatures(),
        userId);

    return ResponseEntity.ok(updatedLicense);
  }

  @GetMapping("/{id}/token")
  public ResponseEntity<TokenResponse> getSignedLicenseToken(@PathVariable UUID id) {
    String token = licenseManagementService.getSignedLicenseToken(id);
    return ResponseEntity.ok(new TokenResponse(token));
  }

  public static class CreateLicenseRequest {
    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    private String customerEmail;

    @NotNull(message = "Expiration date is required")
    @Future(message = "Expiration date must be in the future")
    private Instant expiresAt;

    private Map<String, Object> features;

    public UUID getOrganizationId() {
      return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
      this.organizationId = organizationId;
    }

    public String getProductName() {
      return productName;
    }

    public void setProductName(String productName) {
      this.productName = productName;
    }

    public String getCustomerEmail() {
      return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
      this.customerEmail = customerEmail;
    }

    public Instant getExpiresAt() {
      return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
      this.expiresAt = expiresAt;
    }

    public Map<String, Object> getFeatures() {
      return features;
    }

    public void setFeatures(Map<String, Object> features) {
      this.features = features;
    }
  }

  public static class ExtendLicenseRequest {
    @NotNull(message = "New expiry date is required")
    @Future(message = "New expiry date must be in the future")
    private Instant newExpiryDate;

    public Instant getNewExpiryDate() {
      return newExpiryDate;
    }

    public void setNewExpiryDate(Instant newExpiryDate) {
      this.newExpiryDate = newExpiryDate;
    }
  }

  public static class UpdateLicenseFeaturesRequest {
    @NotNull(message = "Features are required")
    private Map<String, Object> features;

    public Map<String, Object> getFeatures() {
      return features;
    }

    public void setFeatures(Map<String, Object> features) {
      this.features = features;
    }
  }

  public static class TokenResponse {
    private final String token;

    public TokenResponse(String token) {
      this.token = token;
    }

    public String getToken() {
      return token;
    }
  }
}
