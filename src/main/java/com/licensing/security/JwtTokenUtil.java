package com.licensing.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenUtil {

  private final String secret;
  private final long expirationTimeInMillis;

  public JwtTokenUtil(
      @Value("${security.jwt.secret:default-secret-key-that-is-long-enough-for-hmac-256-algorithm}") String secret,
      @Value("${security.jwt.expiration:3600000}") long expirationTimeInMillis) {
    this.secret = secret;
    this.expirationTimeInMillis = expirationTimeInMillis;
  }

  public String generateToken(String userId, String tenantId, String role) {
    try {
      JWSSigner signer = new MACSigner(secret.getBytes());

      Instant now = Instant.now();
      JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
          .subject(userId)
          .claim("tenant_id", tenantId)
          .claim("role", role)
          .issueTime(Date.from(now))
          .expirationTime(Date.from(now.plus(expirationTimeInMillis, ChronoUnit.MILLIS)))
          .build();

      SignedJWT signedJWT = new SignedJWT(
          new JWSHeader(JWSAlgorithm.HS256),
          claimsSet);

      signedJWT.sign(signer);
      return signedJWT.serialize();
    } catch (JOSEException e) {
      throw new RuntimeException("Failed to generate JWT token", e);
    }
  }

  public JWTClaimsSet validateToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      JWSVerifier verifier = new MACVerifier(secret.getBytes());

      if (!signedJWT.verify(verifier)) {
        throw new RuntimeException("Invalid JWT signature");
      }

      JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

      if (claimsSet.getExpirationTime().before(new Date())) {
        throw new RuntimeException("JWT token has expired");
      }

      return claimsSet;
    } catch (ParseException | JOSEException e) {
      throw new RuntimeException("Failed to validate JWT token", e);
    }
  }

  public String extractUserId(String token) {
    JWTClaimsSet claimsSet = validateToken(token);
    return claimsSet.getSubject();
  }

  public String extractTenantId(String token) {
    try {
      JWTClaimsSet claimsSet = validateToken(token);
      return claimsSet.getStringClaim("tenant_id");
    } catch (ParseException e) {
      throw new RuntimeException("Failed to extract tenant ID from JWT token", e);
    }
  }

  public String extractRole(String token) {
    try {
      JWTClaimsSet claimsSet = validateToken(token);
      return claimsSet.getStringClaim("role");
    } catch (ParseException e) {
      throw new RuntimeException("Failed to extract role from JWT token", e);
    }
  }

  public boolean isTokenExpired(String token) {
    try {
      JWTClaimsSet claimsSet = validateToken(token);
      return claimsSet.getExpirationTime().before(new Date());
    } catch (Exception e) {
      return true;
    }
  }
}