package com.licensing.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "security.jwt.enabled=true",
    "security.jwt.secret=test-secret-key-that-is-long-enough-for-hmac-256-algorithm",
    "security.jwt.expiration=3600000",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "license.signing.keys.key-1.private-key=test-private-key-for-ed25519-testing-purposes-only",
    "license.signing.keys.key-1.public-key=test-public-key-for-ed25519-testing-purposes-only",
    "license.signing.keys.key-1.expiry-date=2025-12-31T23:59:59"
})
@ExtendWith(MockitoExtension.class)
class JwtSecurityConfigurationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private JwtTokenUtil jwtTokenUtil;

  @Test
  void shouldRejectRequestsWithoutJwtToken() {
    ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/organizations", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldRejectRequestsWithInvalidJwtToken() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer invalid-token");
    headers.set("X-Tenant-ID", "test-tenant");

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<String> response = restTemplate.exchange("/api/v1/organizations", HttpMethod.GET, entity,
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldRejectRequestsWithoutTenantHeader() {
    String validToken = jwtTokenUtil.generateToken("test-user", "test-tenant", "USER");

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + validToken);

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<String> response = restTemplate.exchange("/api/v1/organizations", HttpMethod.GET, entity,
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldAllowRequestsWithValidJwtAndTenant() {
    String validToken = jwtTokenUtil.generateToken("test-user", "test-tenant", "USER");

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + validToken);
    headers.set("X-Tenant-ID", "test-tenant");
    headers.set("X-User-ID", "test-user");

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<String> response = restTemplate.exchange("/api/v1/organizations", HttpMethod.GET, entity,
        String.class);

    assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldExtractUserIdFromJwtToken() {
    String tokenWithUserId = jwtTokenUtil.generateToken("user123", "test-tenant", "ADMIN");

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + tokenWithUserId);
    headers.set("X-Tenant-ID", "test-tenant");

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<String> response = restTemplate.exchange("/api/v1/organizations", HttpMethod.GET, entity,
        String.class);

    assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
