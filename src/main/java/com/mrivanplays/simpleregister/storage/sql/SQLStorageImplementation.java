package com.mrivanplays.simpleregister.storage.sql;

import com.mrivanplays.simpleregister.storage.PasswordEntry;
import com.mrivanplays.simpleregister.storage.StorageImplementation;
import com.mrivanplays.simpleregister.storage.sql.factory.SQLConnectionFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLStorageImplementation implements StorageImplementation {

  private SQLConnectionFactory connectionFactory;

  public SQLStorageImplementation(SQLConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  @Override
  public void connect() {
    try {
      connectionFactory.connect();
      PreparedStatement statement =
          connectionFactory
              .getConnection()
              .prepareStatement(
                  "CREATE TABLE IF NOT EXISTS simpleregister_passwords(name VARCHAR(255), uuid VARCHAR(255), ip VARCHAR(255), password VARCHAR(255));");
      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public List<PasswordEntry> getPasswords() {
    List<PasswordEntry> entries = new ArrayList<>();
    try {
      PreparedStatement statement =
          connectionFactory
              .getConnection()
              .prepareStatement("SELECT * FROM simpleregister_passwords");
      ResultSet result = statement.executeQuery();
      while (result.next()) {
        entries.add(
            new PasswordEntry(
                result.getString("name"),
                UUID.fromString(result.getString("uuid")),
                result.getString("ip"),
                result.getString("password")));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return entries;
  }

  @Override
  public void addPassword(PasswordEntry entry) {
    try {
      PreparedStatement statement =
          connectionFactory
              .getConnection()
              .prepareStatement(
                  "INSERT INTO simpleregister_passwords (name, uuid, ip, password) VALUES (?, ?, ?, ?)");
      statement.setString(1, entry.getName());
      statement.setString(2, entry.getPlayerUUID().toString());
      statement.setString(3, entry.getPlayerIP());
      statement.setString(4, entry.getPassword());
      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void modifyPassword(UUID owner, PasswordEntry entry) {
    try {
      PreparedStatement statement =
          connectionFactory
              .getConnection()
              .prepareStatement("UPDATE simpleregister_passwords SET password = ? WHERE uuid = ?");
      statement.setString(1, entry.getPassword());
      statement.setString(2, owner.toString());
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void removeEntry(UUID owner) {
    try {
      PreparedStatement statement =
          connectionFactory
              .getConnection()
              .prepareStatement("DELETE FROM simpleregister_passwords WHERE uuid = ?");
      statement.setString(1, owner.toString());
      statement.executeUpdate();
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
}
