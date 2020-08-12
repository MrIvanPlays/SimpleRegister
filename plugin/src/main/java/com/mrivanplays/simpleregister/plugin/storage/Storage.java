package com.mrivanplays.simpleregister.plugin.storage;

import co.aikar.taskchain.TaskChain;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mrivanplays.simpleregister.plugin.DatabaseCredentials;
import com.mrivanplays.simpleregister.plugin.SimpleRegisterPlugin;
import com.mrivanplays.simpleregister.plugin.storage.flatfile.FlatfileStorage;
import com.mrivanplays.simpleregister.plugin.storage.sql.SQLStorageImplementation;
import com.mrivanplays.simpleregister.plugin.storage.sql.factory.connection.MariaDBConnectionFactory;
import com.mrivanplays.simpleregister.plugin.storage.sql.factory.connection.MySQLConnectionFactory;
import com.mrivanplays.simpleregister.plugin.storage.sql.factory.connection.PostgreSQLConnectionFactory;
import com.mrivanplays.simpleregister.plugin.storage.sql.factory.flatfile.H2ConnectionFactory;
import com.mrivanplays.simpleregister.plugin.storage.sql.factory.flatfile.SQLiteConnectionFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Storage {

  private final SimpleRegisterPlugin plugin;
  private final StorageImplementation provider;

  private Cache<UUID, PasswordEntry> cache =
      CacheBuilder.newBuilder().expireAfterWrite(90, TimeUnit.SECONDS).build();
  private Set<PasswordEntry> cachedAllPasswords = new HashSet<>();

  public Storage(SimpleRegisterPlugin plugin) {
    this.plugin = plugin;
    provider = supplyDrop();
    plugin
        .getServer()
        .getScheduler()
        .scheduleSyncRepeatingTask(plugin, () -> cachedAllPasswords.clear(), 600, 400); // 20s
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

  public void getPasswordEntry(UUID uuid, Consumer<PasswordEntry> callback) {
    PasswordEntry cached = cache.getIfPresent(uuid);
    if (cached != null) {
      callback.accept(cached);
      cachedAllPasswords.add(cached);
      return;
    }
    if (cachedAllPasswords.isEmpty()) {
      TaskChain<?> chain = plugin.newSharedChain("getPasswordEntry");
      chain
          .async(() -> chain.setTaskData("everything", provider.getPasswords()))
          .sync(
              () -> {
                cachedAllPasswords.addAll(chain.getTaskData("everything"));

                for (PasswordEntry entry : cachedAllPasswords) {
                  if (entry.getPlayerUUID().equals(uuid)) {
                    callback.accept(entry);
                    break;
                  }
                }
              })
          .execute();
      return;
    }
    for (PasswordEntry entry : cachedAllPasswords) {
      if (entry.getPlayerUUID().equals(uuid)) {
        callback.accept(entry);
        break;
      }
    }
  }

  public void getAltAccounts(String ip, Consumer<List<PasswordEntry>> callback) {
    if (cachedAllPasswords.isEmpty()) {
      TaskChain<?> chain = plugin.newSharedChain("getAltAccounts");
      chain
          .async(() -> chain.setTaskData("everything", provider.getPasswords()))
          .sync(
              () -> {
                cachedAllPasswords.addAll(chain.getTaskData("everything"));

                List<PasswordEntry> ret = new ArrayList<>();
                for (PasswordEntry entry : cachedAllPasswords) {
                  if (entry.getPlayerIP().equalsIgnoreCase(ip)) {
                    ret.add(entry);
                  }
                }
                callback.accept(ret);
              })
          .execute();
      return;
    }

    List<PasswordEntry> ret = new ArrayList<>();
    for (PasswordEntry entry : cachedAllPasswords) {
      if (entry.getPlayerIP().equalsIgnoreCase(ip)) {
        ret.add(entry);
      }
    }
    callback.accept(ret);
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
    StorageType storageType =
        StorageType.valueOf(plugin.getConfiguration().getString("database.type").toUpperCase());
    switch (storageType) {
      case H2:
        return new SQLStorageImplementation(new H2ConnectionFactory(plugin), storageType, plugin);
      case MYSQL:
        return new SQLStorageImplementation(
            new MySQLConnectionFactory(credentials), storageType, plugin);
      case SQLITE:
        return new SQLStorageImplementation(
            new SQLiteConnectionFactory(plugin), storageType, plugin);
      case MARIADB:
        return new SQLStorageImplementation(
            new MariaDBConnectionFactory(credentials), storageType, plugin);
      case POSTGRESQL:
        return new SQLStorageImplementation(
            new PostgreSQLConnectionFactory(credentials), storageType, plugin);
      default:
        return new FlatfileStorage(plugin.getDataFolder());
    }
  }
}
