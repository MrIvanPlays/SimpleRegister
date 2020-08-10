package com.mrivanplays.simpleregister.plugin;

import com.mrivanplays.simpleregister.plugin.config.Configuration;

public final class DatabaseCredentials {

  private String username;
  private String password;
  private String databaseName;
  private String ip;
  private int port;

  public DatabaseCredentials(Configuration config) {
    this.username = config.getString("database.username");
    this.password = config.getString("database.password");
    this.databaseName = config.getString("database.name");
    this.ip = config.getString("database.ip");
    this.port = config.getInt("database.port");
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public String getIp() {
    return ip;
  }

  public int getPort() {
    return port;
  }
}
