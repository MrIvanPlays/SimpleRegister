package com.mrivanplays.simpleregister.storage;

import co.aikar.taskchain.TaskChain;
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
import java.util.function.Consumer;

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
    plugin
        .getServer()
        .getScheduler()
        .runTaskAsynchronously(plugin, () -> provider.addPassword(entry));
  }

  public void getPasswords(Consumer<List<PasswordEntry>> callback) {
    TaskChain<?> chain = plugin.newSharedChain("getPasswords");
    chain
        .async(() -> chain.setTaskData("passwords", provider.getPasswords()))
        .sync(() -> callback.accept(chain.getTaskData("passwords")))
        .execute();
  }

  public void getPasswordEntry(UUID uuid, Consumer<PasswordEntry> callback) {
    TaskChain<?> chain = plugin.newSharedChain("getPasswordEntry");
    chain
        .async(() -> chain.setTaskData("entry", provider.getPasswordEntry(uuid)))
        .sync(() -> callback.accept(chain.getTaskData("entry")))
        .execute();
  }

  public void getPasswordEntrySync(UUID uuid, Consumer<PasswordEntry> callback) {
    callback.accept(provider.getPasswordEntry(uuid));
  }

  public void getAltAccounts(String ip, Consumer<List<PasswordEntry>> callback) {
    TaskChain<?> chain = plugin.newSharedChain("getAltAccounts");
    chain
        .async(() -> chain.setTaskData("altAccounts", provider.getAltAccounts(ip)))
        .sync(() -> callback.accept(chain.getTaskData("altAccounts")))
        .execute();
  }

  public void modifyPassword(UUID uuid, PasswordEntry entry) {
    plugin
        .getServer()
        .getScheduler()
        .runTaskAsynchronously(plugin, () -> provider.modifyPassword(uuid, entry));
  }

  public void removeEntry(UUID uuid) {
    plugin
        .getServer()
        .getScheduler()
        .runTaskAsynchronously(plugin, () -> provider.removeEntry(uuid));
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
