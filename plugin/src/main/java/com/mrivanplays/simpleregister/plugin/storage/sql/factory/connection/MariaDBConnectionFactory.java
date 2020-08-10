package com.mrivanplays.simpleregister.plugin.storage.sql.factory.connection;

import com.mrivanplays.simpleregister.plugin.DatabaseCredentials;
import com.zaxxer.hikari.HikariConfig;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Credits: lucko
 * Source: github.com/lucko/LuckPerms
 *
 * @author lucko
 */
public class MariaDBConnectionFactory extends HikariConnectionFactory {

  public MariaDBConnectionFactory(DatabaseCredentials credentials) {
    super(credentials);
  }

  @Override
  protected String getDriverClass() {
    return "org.mariadb.jdbc.MariaDbDataSource";
  }

  @Override
  protected void appendProperties(HikariConfig config, Map<String, String> properties) {
    if (properties.isEmpty()) {
      return;
    }

    String propertiesString =
        properties.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining(";"));

    config.addDataSourceProperty("properties", propertiesString);
  }
}
