package com.mrivanplays.simpleregister.storage.sql;

import com.mrivanplays.simpleregister.SimpleRegister;
import com.mrivanplays.simpleregister.storage.PasswordEntry;
import com.mrivanplays.simpleregister.storage.StorageImplementation;
import com.mrivanplays.simpleregister.storage.StorageType;
import com.mrivanplays.simpleregister.storage.sql.factory.SQLConnectionFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLStorageImplementation implements StorageImplementation {

  private SQLConnectionFactory connectionFactory;
  private StorageType storageType;
  private SimpleRegister plugin;

  public SQLStorageImplementation(
      SQLConnectionFactory connectionFactory, StorageType storageType, SimpleRegister plugin) {
    this.connectionFactory = connectionFactory;
    this.storageType = storageType;
    this.plugin = plugin;
  }

  @Override
  public void connect() {
    try {
      connectionFactory.connect();
      try (Connection connection = connectionFactory.getConnection()) {
        boolean tableExists = tableExists(connection, "simpleregister_passwords");
        if (!tableExists) {
          try (InputStream in =
              plugin.getResource(
                  "com/mrivanplays/simpleregister/sql/schemas/"
                      + storageType.name().toLowerCase()
                      + ".sql")) {
            boolean utf8mb4Unsupported = false;
            List<String> queries = SchemaReader.getStatements(in);
            try (Statement s = connection.createStatement()) {
              for (String query : queries) {
                s.addBatch(query);
              }

              try {
                s.executeBatch();
              } catch (BatchUpdateException e) {
                if (e.getMessage().contains("Unknown character set")) {
                  utf8mb4Unsupported = true;
                } else {
                  throw e;
                }
              }
            }

            if (utf8mb4Unsupported) {
              try (Statement s = connection.createStatement()) {
                for (String query : queries) {
                  s.addBatch(query.replace("utf8mb4", "utf8"));
                }

                s.executeBatch();
              }
            }
          }
        }
      }
    } catch (IOException | SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public List<PasswordEntry> getPasswords() {
    List<PasswordEntry> entries = new ArrayList<>();
    try {
      try (Connection connection = connectionFactory.getConnection()) {
        try (PreparedStatement statement =
            connection.prepareStatement("SELECT * FROM simpleregister_passwords")) {
          try (ResultSet result = statement.executeQuery()) {
            while (result.next()) {
              entries.add(
                  new PasswordEntry(
                      result.getInt("id"),
                      result.getString("name"),
                      UUID.fromString(result.getString("uuid")),
                      result.getString("ip"),
                      result.getString("password")));
            }
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return entries;
  }

  @Override
  public void addPassword(PasswordEntry entry) {
    try {
      try (Connection connection = connectionFactory.getConnection()) {
        try (PreparedStatement statement =
            connection.prepareStatement(
                "INSERT INTO simpleregister_passwords (name, uuid, ip, password) VALUES (?, ?, ?, ?)")) {
          statement.setString(1, entry.getName());
          statement.setString(2, entry.getPlayerUUID().toString());
          statement.setString(3, entry.getPlayerIP());
          statement.setString(4, entry.getPassword());
          statement.executeUpdate();
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void modifyPassword(UUID owner, PasswordEntry entry) {
    try {
      try (Connection connection = connectionFactory.getConnection()) {
        try (PreparedStatement statement =
            connection.prepareStatement(
                "UPDATE simpleregister_passwords SET password = ? WHERE uuid = ?")) {
          statement.setString(1, entry.getPassword());
          statement.setString(2, owner.toString());
          statement.executeUpdate();
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void removeEntry(UUID owner) {
    try {
      try (Connection connection = connectionFactory.getConnection()) {
        try (PreparedStatement statement =
            connection.prepareStatement("DELETE FROM simpleregister_passwords WHERE uuid = ?")) {
          statement.setString(1, owner.toString());
          statement.executeUpdate();
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() {
    try {
      connectionFactory.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private static boolean tableExists(Connection connection, String table) throws SQLException {
    try (ResultSet rs = connection.getMetaData().getTables(null, null, "%", null)) {
      while (rs.next()) {
        if (rs.getString(3).equalsIgnoreCase(table)) {
          return true;
        }
      }
      return false;
    }
  }
}
