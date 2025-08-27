package com.licensing.config.tenant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantConnectionProviderTest {

  @Mock
  private DataSource dataSource;

  @Mock
  private Connection connection;

  @InjectMocks
  private TenantConnectionProvider connectionProvider;

  @Test
  void shouldReturnTrueForSupportsAggressiveRelease() {
    assertThat(connectionProvider.supportsAggressiveRelease()).isTrue();
  }

  @Test
  void shouldGetAnyConnectionFromDataSource() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);

    Connection result = connectionProvider.getAnyConnection();

    assertThat(result).isEqualTo(connection);
    verify(dataSource).getConnection();
  }

  @Test
  void shouldReleaseAnyConnection() throws SQLException {
    connectionProvider.releaseAnyConnection(connection);

    verify(connection).close();
  }

  @Test
  void shouldGetConnectionForTenant() throws SQLException {
    String tenantId = "tenant-123";
    when(dataSource.getConnection()).thenReturn(connection);

    Connection result = connectionProvider.getConnection(tenantId);

    assertThat(result).isEqualTo(connection);
    verify(dataSource).getConnection();
  }

  @Test
  void shouldReleaseConnectionForTenant() throws SQLException {
    String tenantId = "tenant-123";

    connectionProvider.releaseConnection(tenantId, connection);

    verify(connection).close();
  }

  @Test
  void shouldReturnFalseForIsUnwrappableAs() {
    assertThat(connectionProvider.isUnwrappableAs(DataSource.class)).isFalse();
    assertThat(connectionProvider.isUnwrappableAs(Connection.class)).isFalse();
  }
}
