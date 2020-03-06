package com.mrivanplays.simpleregister.storage.sql.factory.connection;

import com.mrivanplays.simpleregister.DatabaseCredentials;
import com.zaxxer.hikari.HikariConfig;
import java.util.Map;

/**
 * Credits: lucko
 * Source: github.com/lucko/LuckPerms
 *
 * @author lucko
 */
public class MySQLConnectionFactory extends HikariConnectionFactory {

  public MySQLConnectionFactory(DatabaseCredentials credentials) {
    super(credentials);
  }

  @Override
  protected String getDriverClass() {
    return "com.mysql.jdbc.jdbc2.optional.MysqlDataSource";
  }

  @Override
  protected void appendProperties(HikariConfig config, Map<String, String> properties) {
    properties.putIfAbsent("cachePrepStmts", "false");
    properties.putIfAbsent("cacheServerConfiguration", "true");

    super.appendProperties(config, properties);
  }
}
