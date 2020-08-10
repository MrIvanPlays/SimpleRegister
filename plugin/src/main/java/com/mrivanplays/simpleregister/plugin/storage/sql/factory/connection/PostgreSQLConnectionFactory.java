package com.mrivanplays.simpleregister.plugin.storage.sql.factory.connection;

import com.mrivanplays.simpleregister.plugin.DatabaseCredentials;
import com.zaxxer.hikari.HikariConfig;
import java.util.Map;

/**
 * Credits: lucko
 * Source: github.com/lucko/LuckPerms
 *
 * @author lucko
 */
public class PostgreSQLConnectionFactory extends HikariConnectionFactory {

  public PostgreSQLConnectionFactory(DatabaseCredentials credentials) {
    super(credentials);
  }

  @Override
  protected void appendProperties(HikariConfig config, Map<String, String> properties) {
    properties.remove("useUnicode");
    properties.remove("characterEncoding");

    super.appendProperties(config, properties);
  }

  @Override
  protected void appendConfigurationInfo(HikariConfig config) {
    config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
    config.addDataSourceProperty("serverName", credentials.getIp());
    config.addDataSourceProperty("portNumber", credentials.getPort());
    config.addDataSourceProperty("databaseName", credentials.getDatabaseName());
    config.addDataSourceProperty("user", credentials.getUsername());
    config.addDataSourceProperty("password", credentials.getPassword());
  }
}
