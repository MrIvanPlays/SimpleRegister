package com.mrivanplays.simpleregister.plugin.storage;

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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class Storage {

  private final SimpleRegisterPlugin plugin;
  private final StorageImplementation provider;

  private Cache<UUID, PasswordEntry> cache =
      CacheBuilder.newBuilder().expireAfterWrite(90, TimeUnit.SECONDS).build();
  private Set<PasswordEntry> cachedAllPasswords = new HashSet<>();
  private Lock passwordsLock = new ReentrantLock();
  private final ScheduledExecutorService ASYNC_OPERATIONS =
      Executors.newSingleThreadScheduledExecutor();

  public Storage(SimpleRegisterPlugin plugin) {
    this.plugin = plugin;
    provider = supplyDrop();
    ASYNC_OPERATIONS.scheduleAtFixedRate(
        () -> {
          passwordsLock.lock();
          try {
            cachedAllPasswords.clear();
          } finally {
            passwordsLock.unlock();
          }
        },
        20,
        20,
        TimeUnit.SECONDS);
  }

  public void connect() {
    provider.connect();
  }

  public void close() {
    provider.close();
  }

  public void addPassword(PasswordEntry entry) {
    ASYNC_OPERATIONS.submit(() -> provider.addPassword(entry));
  }

  public void getPasswordEntry(UUID uuid, Consumer<PasswordEntry> callback) {
    PasswordEntry cached = cache.getIfPresent(uuid);
    if (cached != null) {
      callback.accept(cached);
      cachedAllPasswords.add(cached);
      return;
    }
    if (cachedAllPasswords.isEmpty()) {
      ASYNC_OPERATIONS.submit(
          () -> {
            List<PasswordEntry> passwords = provider.getPasswords();
            for (PasswordEntry entry : passwords) {
              if (entry.getPlayerUUID().equals(uuid)) {
                callback.accept(entry);
                break;
              }
            }
            plugin
                .getServer()
                .getScheduler()
                .runTask(
                    plugin,
                    () -> {
                      passwordsLock.lock();
                      try {
                        cachedAllPasswords.addAll(passwords);
                      } finally {
                        passwordsLock.unlock();
                      }
                    });
          });
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
      ASYNC_OPERATIONS.submit(
          () -> {
            List<PasswordEntry> passwords = provider.getPasswords();
            List<PasswordEntry> alts = new ArrayList<>();
            for (PasswordEntry entry : passwords) {
              if (entry.getPlayerIP().equalsIgnoreCase(ip)) {
                alts.add(entry);
              }
            }
            plugin
                .getServer()
                .getScheduler()
                .runTask(
                    plugin,
                    () -> {
                      callback.accept(alts);
                      passwordsLock.lock();
                      try {
                        cachedAllPasswords.addAll(passwords);
                      } finally {
                        passwordsLock.unlock();
                      }
                    });
          });
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
    ASYNC_OPERATIONS.submit(() -> provider.modifyPassword(uuid, entry));
  }

  public void removeEntry(UUID uuid) {
    ASYNC_OPERATIONS.submit(() -> provider.removeEntry(uuid));
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
