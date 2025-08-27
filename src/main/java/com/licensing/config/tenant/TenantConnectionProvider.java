package com.licensing.config.tenant;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Multi-tenant connection provider that uses schema-based multi-tenancy.
 * Each tenant gets its own database schema.
 */
@Component
public class TenantConnectionProvider extends AbstractMultiTenantConnectionProvider<String>
    implements HibernatePropertiesCustomizer {

  private final DataSource dataSource;

  public TenantConnectionProvider(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  protected ConnectionProvider getAnyConnectionProvider() {
    return new DataSourceConnectionProvider(dataSource);
  }

  @Override
  protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
    return new DataSourceConnectionProvider(dataSource);
  }

  @Override
  public void customize(Map<String, Object> hibernateProperties) {
    hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, this);
  }

  /**
   * Simple ConnectionProvider implementation that wraps a DataSource.
   */
  private static class DataSourceConnectionProvider implements ConnectionProvider {

    private final DataSource dataSource;

    public DataSourceConnectionProvider(DataSource dataSource) {
      this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
      return dataSource.getConnection();
    }

    @Override
    public void closeConnection(Connection conn) throws SQLException {
      conn.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
      return true;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
      return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
      return null;
    }
  }
}
