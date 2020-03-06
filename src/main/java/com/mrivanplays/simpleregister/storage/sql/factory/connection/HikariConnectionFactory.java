package com.mrivanplays.simpleregister.storage.sql.factory.connection;

import com.mrivanplays.simpleregister.DatabaseCredentials;
import com.mrivanplays.simpleregister.storage.sql.factory.SQLConnectionFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Credits: lucko
 * Source: github.com/lucko/LuckPerms
 *
 * @author lucko
 */
public abstract class HikariConnectionFactory implements SQLConnectionFactory {

  private HikariDataSource hikari;
  protected DatabaseCredentials credentials;

  public HikariConnectionFactory(DatabaseCredentials credentials) {
    this.credentials = credentials;
  }

  protected String getDriverClass() {
    return null;
  }

  protected void appendProperties(HikariConfig config, Map<String, String> properties) {
    for (Map.Entry<String, String> propertyEntry : properties.entrySet()) {
      config.addDataSourceProperty(propertyEntry.getKey(), propertyEntry.getValue());
    }
  }

  protected void appendConfigurationInfo(HikariConfig config) {
    config.setDataSourceClassName(getDriverClass());
    config.addDataSourceProperty("serverName", credentials.getIp());
    config.addDataSourceProperty("port", credentials.getPort());
    config.addDataSourceProperty("databaseName", credentials.getDatabaseName());
    config.setUsername(credentials.getUsername());
    config.setPassword(credentials.getPassword());
  }

  @Override
  public void connect() throws SQLException {
    HikariConfig config = new HikariConfig();
    config.setPoolName("networkbans-hikari");
    appendConfigurationInfo(config);

    Map<String, String> properties = new HashMap<>();
    properties.put("useUnicode", "true");
    properties.put("characterEncoding", "utf8");
    appendProperties(config, properties);

    hikari = new HikariDataSource(config);
  }

  @Override
  public Connection getConnection() throws SQLException {
    return hikari.getConnection();
  }

  @Override
  public void close() throws SQLException {
    hikari.close();
  }
}
