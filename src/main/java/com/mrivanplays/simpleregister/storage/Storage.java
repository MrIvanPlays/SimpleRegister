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
import com.mrivanplays.simpleregister.util.ThrowableRunnable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public class Storage {

  private SimpleRegister plugin;
  private Executor executor;
  private StorageImplementation provider;

  public Storage(SimpleRegister plugin, Executor executor) {
    this.plugin = plugin;
    this.executor = executor;
    provider = supplyDrop();
  }

  public void connect() {
    provider.connect();
  }

  public void close() {
    provider.close();
  }

  public CompletableFuture<Void> addPassword(PasswordEntry entry) {
    return makeFuture(() -> provider.addPassword(entry));
  }

  public CompletableFuture<List<PasswordEntry>> getPasswords() {
    return makeFuture(() -> provider.getPasswords());
  }

  public CompletableFuture<PasswordEntry> getPasswordEntry(UUID uuid) {
    return makeFuture(() -> provider.getPasswordEntry(uuid));
  }

  public CompletableFuture<List<PasswordEntry>> getAltAccounts(String ip) {
    return makeFuture(() -> provider.getAltAccounts(ip));
  }

  public CompletableFuture<Void> modifyPassword(UUID uuid, PasswordEntry entry) {
    return makeFuture(() -> provider.modifyPassword(uuid, entry));
  }

  public CompletableFuture<Void> removeEntry(UUID uuid) {
    return makeFuture(() -> provider.removeEntry(uuid));
  }

  private <T> CompletableFuture<T> makeFuture(Callable<T> supplier) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return supplier.call();
          } catch (Exception e) {
            if (e instanceof RuntimeException) {
              throw (RuntimeException) e;
            }
            throw new CompletionException(e);
          }
        },
        executor);
  }

  private CompletableFuture<Void> makeFuture(ThrowableRunnable runnable) {
    return CompletableFuture.runAsync(
        () -> {
          try {
            runnable.run();
          } catch (Exception e) {
            if (e instanceof RuntimeException) {
              throw (RuntimeException) e;
            }
            throw new CompletionException(e);
          }
        },
        executor);
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
