package com.licensing.service.cache;

import com.licensing.config.cache.CacheConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service providing caching operations and cache management utilities.
 * Demonstrates proper usage of different cache types with eviction strategies.
 */
@Service
public class CacheService {

  private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

  private final CacheManager cacheManager;

  public CacheService(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  /**
   * Caches organization data with tenant-aware keys.
   */
  @Cacheable(value = CacheConfiguration.ORGANIZATIONS_CACHE, key = "#tenantId + ':' + #orgId")
  public Optional<Object> getOrganizationFromCache(String tenantId, String orgId) {
    logger.debug("Cache miss for organization: {} in tenant: {}", orgId, tenantId);
    return Optional.empty();
  }

  /**
   * Evicts organization cache when organization is updated.
   */
  @CacheEvict(value = CacheConfiguration.ORGANIZATIONS_CACHE, key = "#tenantId + ':' + #orgId")
  public void evictOrganizationCache(String tenantId, String orgId) {
    logger.info("Evicted organization cache for org: {} in tenant: {}", orgId, tenantId);
  }

  /**
   * Caches license data with expiration.
   */
  @Cacheable(value = CacheConfiguration.LICENSES_CACHE, key = "#tenantId + ':' + #licenseId")
  public Optional<Object> getLicenseFromCache(String tenantId, String licenseId) {
    logger.debug("Cache miss for license: {} in tenant: {}", licenseId, tenantId);
    return Optional.empty();
  }

  /**
   * Evicts all license caches for a tenant when licenses are bulk updated.
   */
  @CacheEvict(value = CacheConfiguration.LICENSES_CACHE, allEntries = true, condition = "#tenantId != null")
  public void evictAllLicenseCaches(String tenantId) {
    logger.info("Evicted all license caches for tenant: {}", tenantId);
  }

  /**
   * Caches user session data.
   */
  @Cacheable(value = CacheConfiguration.USER_SESSIONS_CACHE, key = "#sessionId")
  public Optional<Object> getUserSessionFromCache(String sessionId) {
    logger.debug("Cache miss for user session: {}", sessionId);
    return Optional.empty();
  }

  /**
   * Evicts user session when user logs out.
   */
  @CacheEvict(value = CacheConfiguration.USER_SESSIONS_CACHE, key = "#sessionId")
  public void evictUserSessionCache(String sessionId) {
    logger.info("Evicted user session cache: {}", sessionId);
  }

  /**
   * Caches API responses for repeated requests.
   */
  @Cacheable(value = CacheConfiguration.API_RESPONSES_CACHE, key = "#endpoint + ':' + #params.hashCode()")
  public Optional<Object> getApiResponseFromCache(String endpoint, Object params) {
    logger.debug("Cache miss for API response: {} with params: {}", endpoint, params);
    return Optional.empty();
  }

  /**
   * Gets cache statistics for monitoring.
   */
  public List<CacheStats> getCacheStatistics() {
    List<CacheStats> stats = new ArrayList<>();

    for (String cacheName : getCacheNames()) {
      Cache cache = cacheManager.getCache(cacheName);
      if (cache != null) {
        int size = 0;
        double hitRate = 0.0;
        stats.add(new CacheStats(cacheName, size, hitRate));
      }
    }

    return stats;
  }

  /**
   * Clears all caches - use with caution in production.
   */
  public void clearAllCaches() {
    for (String cacheName : getCacheNames()) {
      Cache cache = cacheManager.getCache(cacheName);
      if (cache != null) {
        cache.clear();
        logger.warn("Cleared cache: {}", cacheName);
      }
    }
  }

  private String[] getCacheNames() {
    return new String[] {
        CacheConfiguration.ORGANIZATIONS_CACHE,
        CacheConfiguration.LICENSES_CACHE,
        CacheConfiguration.USER_SESSIONS_CACHE,
        CacheConfiguration.API_RESPONSES_CACHE,
        CacheConfiguration.TENANT_METADATA_CACHE
    };
  }

  public static class CacheStats {
    private final String name;
    private final int size;
    private final double hitRate;

    public CacheStats(String name, int size, double hitRate) {
      this.name = name;
      this.size = size;
      this.hitRate = hitRate;
    }

    public String getName() {
      return name;
    }

    public int getSize() {
      return size;
    }

    public double getHitRate() {
      return hitRate;
    }

    @Override
    public String toString() {
      return String.format("Cache[%s]: size=%d, hitRate=%.2f%%", name, size, hitRate * 100);
    }
  }
}
