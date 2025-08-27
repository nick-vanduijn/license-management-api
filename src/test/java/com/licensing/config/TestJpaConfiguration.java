package com.licensing.config;

import com.licensing.config.tenant.CurrentTenantResolver;
import com.licensing.config.tenant.TenantConnectionProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Test configuration to provide tenant-related beans for testing with H2
 * database.
 * This ensures the tenant providers are available as Spring beans.
 */
@TestConfiguration
@Profile("test")
public class TestJpaConfiguration {

    @Bean
    @Primary
    public TenantConnectionProvider testTenantConnectionProvider(DataSource dataSource) {
        return new TenantConnectionProvider(dataSource);
    }

    @Bean
    @Primary
    public CurrentTenantResolver testCurrentTenantResolver() {
        return new CurrentTenantResolver();
    }
}
