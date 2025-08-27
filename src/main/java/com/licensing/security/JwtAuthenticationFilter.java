package com.licensing.security;

import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@ConditionalOnProperty(name = "security.jwt.enabled", havingValue = "true", matchIfMissing = false)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String TENANT_HEADER = "X-Tenant-ID";

  private final JwtTokenUtil jwtTokenUtil;

  public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil) {
    this.jwtTokenUtil = jwtTokenUtil;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
    String tenantId = request.getHeader(TENANT_HEADER);

    if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
      logger.debug("No JWT token found in Authorization header");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("{\"error\":\"Missing or invalid Authorization header\"}");
      return;
    }

    if (tenantId == null || tenantId.trim().isEmpty()) {
      logger.debug("No tenant ID found in X-Tenant-ID header");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("{\"error\":\"Missing X-Tenant-ID header\"}");
      return;
    }

    String token = authorizationHeader.substring(BEARER_PREFIX.length());

    try {
      JWTClaimsSet claimsSet = jwtTokenUtil.validateToken(token);
      String userId = claimsSet.getSubject();
      String role = claimsSet.getStringClaim("role");
      String tokenTenantId = claimsSet.getStringClaim("tenant_id");

      if (!tenantId.equals(tokenTenantId)) {
        logger.warn("Tenant ID mismatch: header={}, token={}", tenantId, tokenTenantId);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("{\"error\":\"Tenant ID mismatch\"}");
        return;
      }

      if (SecurityContextHolder.getContext().getAuthentication() == null) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null,
            Collections.singletonList(authority));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        authentication.setDetails(new TenantAwareAuthenticationDetails(request, tenantId));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.debug("JWT authentication successful for user: {} in tenant: {}", userId, tenantId);
      }

    } catch (Exception e) {
      logger.warn("JWT token validation failed: {}", e.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("{\"error\":\"Invalid JWT token\"}");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private static class TenantAwareAuthenticationDetails {
    private final String remoteAddress;
    private final String sessionId;
    private final String tenantId;

    public TenantAwareAuthenticationDetails(HttpServletRequest request, String tenantId) {
      this.remoteAddress = request.getRemoteAddr();
      this.sessionId = request.getSession(false) != null ? request.getSession().getId() : null;
      this.tenantId = tenantId;
    }

    @SuppressWarnings("unused")
    public String getTenantId() {
      return tenantId;
    }

    @SuppressWarnings("unused")
    public String getRemoteAddress() {
      return remoteAddress;
    }

    @SuppressWarnings("unused")
    public String getSessionId() {
      return sessionId;
    }
  }
}
