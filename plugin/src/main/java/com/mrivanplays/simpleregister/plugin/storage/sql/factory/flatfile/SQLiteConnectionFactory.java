package com.mrivanplays.simpleregister.plugin.storage.sql.factory.flatfile;

import com.mrivanplays.simpleregister.plugin.SimpleRegisterPlugin;
import com.mrivanplays.simpleregister.plugin.dependency.Dependency;
import com.mrivanplays.simpleregister.plugin.dependency.classloader.IsolatedClassLoader;
import com.mrivanplays.simpleregister.plugin.storage.sql.factory.SQLConnectionFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Properties;

/**
 * Credits: lucko Source: github.com/lucko/LuckPerms
 *
 * @author lucko
 */
public class SQLiteConnectionFactory implements SQLConnectionFactory {

  private Method createConnectionMethod;
  private Connection connection;
  private File file;

  public SQLiteConnectionFactory(SimpleRegisterPlugin plugin) {
    file = new File(plugin.getDataFolder(), "storage-sqlite.db");
    createFileIfNotExists();

    IsolatedClassLoader classLoader =
        plugin.getDependencyManager().obtainClassLoaderWith(EnumSet.of(Dependency.SQLITE_DRIVER));

    try {
      Class<?> jdbcClass = classLoader.loadClass("org.sqlite.JDBC");
      this.createConnectionMethod =
          jdbcClass.getMethod("createConnection", String.class, Properties.class);
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void connect() throws SQLException {
    try {
      this.connection =
          (Connection)
              createConnectionMethod.invoke(
                  null,
                  "jdbc:sqlite:" + file.getAbsoluteFile().toPath().toString(),
                  new Properties());
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof SQLException) {
        throw (SQLException) e.getCause();
      }
      e.printStackTrace();
    }
  }

  @Override
  public Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {
      try {
        this.connection =
            (Connection)
                createConnectionMethod.invoke(
                    null,
                    "jdbc:sqlite:" + file.getAbsoluteFile().toPath().toString(),
                    new Properties());
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        if (e.getCause() instanceof SQLException) {
          throw (SQLException) e.getCause();
        }
        e.printStackTrace();
      }
    }
    return connection;
  }

  @Override
  public void close() throws SQLException {
    if (connection != null && !connection.isClosed()) {
      connection.close();
    }
  }

  private void createFileIfNotExists() {
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
