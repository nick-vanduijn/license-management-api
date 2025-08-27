package com.licensing.config.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Comprehensive caching configuration for production performance.
 * Implements multi-tier caching with Redis backend and different TTL
 * strategies.
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    public static final String ORGANIZATIONS_CACHE = "organizations";
    public static final String LICENSES_CACHE = "licenses";
    public static final String USER_SESSIONS_CACHE = "user-sessions";
    public static final String API_RESPONSES_CACHE = "api-responses";
    public static final String TENANT_METADATA_CACHE = "tenant-metadata";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put(ORGANIZATIONS_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(2)));

        cacheConfigurations.put(LICENSES_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(60)));

        cacheConfigurations.put(USER_SESSIONS_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(15)));

        cacheConfigurations.put(API_RESPONSES_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        cacheConfigurations.put(TENANT_METADATA_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(6)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    /**
     * Cache configuration properties for different cache types.
     */
    public static class CacheConfig {
        public static final int ORGANIZATIONS_TTL_HOURS = 2;
        public static final int LICENSES_TTL_MINUTES = 60;
        public static final int USER_SESSIONS_TTL_MINUTES = 15;
        public static final int API_RESPONSES_TTL_MINUTES = 5;
        public static final int TENANT_METADATA_TTL_HOURS = 6;

        public static final int MAX_ORGANIZATIONS_CACHE_SIZE = 1000;
        public static final int MAX_LICENSES_CACHE_SIZE = 10000;
        public static final int MAX_USER_SESSIONS_CACHE_SIZE = 5000;
        public static final int MAX_API_RESPONSES_CACHE_SIZE = 2000;
        public static final int MAX_TENANT_METADATA_CACHE_SIZE = 100;
    }
}
