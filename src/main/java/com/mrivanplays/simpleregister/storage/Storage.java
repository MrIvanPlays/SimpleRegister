package com.mrivanplays.simpleregister.storage;

import com.mrivanplays.simpleregister.DatabaseCredentials;
import com.mrivanplays.simpleregister.SimpleRegister;
import com.mrivanplays.simpleregister.storage.flatfile.FlatfileStorage;
import com.mrivanplays.simpleregister.storage.sql.SQLStorageImplementation;
import com.mrivanplays.simpleregister.storage.sql.factory.connection.MariaDBConnectionFactory;
import com.mrivanplays.simpleregister.storage.sql.factory.connection.MySQLConnectionFactory;
import com.mrivanplays.simpleregister.storage.sql.factory.connection.PostgreSQLConnectionFactory;
import com.mrivanplays.simpleregister.storage.sql.factory.flatfile.H2ConnectionFactory;
import com.mrivanplays.simpleregister.storage.sql.factory.flatfile.SQLiteConnectionFactory;
import java.util.List;
import java.util.UUID;

public class Storage {

  private SimpleRegister plugin;
  private StorageImplementation provider;

  public Storage(SimpleRegister plugin) {
    this.plugin = plugin;
    provider = supplyDrop();
  }

  public void connect() {
    provider.connect();
  }

  public void close() {
    provider.close();
  }

  public void addPassword(PasswordEntry entry) {
    provider.addPassword(entry);
  }

  public List<PasswordEntry> getPasswords() {
    return provider.getPasswords();
  }

  public PasswordEntry getPasswordEntry(UUID uuid) {
    return provider.getPasswordEntry(uuid);
  }

  public List<PasswordEntry> getAltAccounts(String ip) {
    return provider.getAltAccounts(ip);
  }

  public void modifyPassword(UUID uuid, PasswordEntry entry) {
    provider.modifyPassword(uuid, entry);
  }

  public void removeEntry(UUID uuid) {
    provider.removeEntry(uuid);
  }

  private StorageImplementation supplyDrop() {
    DatabaseCredentials credentials = new DatabaseCredentials(plugin.getConfiguration());
    switch (StorageType.valueOf(
        plugin.getConfiguration().getString("database.type").toUpperCase())) {
      case H2:
        return new SQLStorageImplementation(new H2ConnectionFactory(plugin));
      case MYSQL:
        return new SQLStorageImplementation(new MySQLConnectionFactory(credentials));
      case SQLITE:
        return new SQLStorageImplementation(new SQLiteConnectionFactory(plugin));
      case MARIADB:
        return new SQLStorageImplementation(new MariaDBConnectionFactory(credentials));
      case POSTGRESQL:
        return new SQLStorageImplementation(new PostgreSQLConnectionFactory(credentials));
      default:
        return new FlatfileStorage(plugin.getDataFolder());
    }
  }
}
