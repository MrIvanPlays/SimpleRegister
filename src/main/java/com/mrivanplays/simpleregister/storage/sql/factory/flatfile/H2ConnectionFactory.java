package com.mrivanplays.simpleregister.storage.sql.factory.flatfile;

import com.mrivanplays.simpleregister.SimpleRegister;
import com.mrivanplays.simpleregister.dependency.Dependency;
import com.mrivanplays.simpleregister.dependency.classloader.IsolatedClassLoader;
import com.mrivanplays.simpleregister.storage.sql.factory.SQLConnectionFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Properties;

/**
 * Credits: lucko Source: github.com/lucko/LuckPerms
 *
 * @author lucko
 */
public class H2ConnectionFactory implements SQLConnectionFactory {

  private Driver driver;
  private Connection connection;
  private Path file;

  public H2ConnectionFactory(SimpleRegister plugin) {
    file = plugin.getDataFolder().getAbsoluteFile().toPath().resolve("storage-h2");
    IsolatedClassLoader classLoader =
        plugin.getDependencyManager().obtainClassLoaderWith(EnumSet.of(Dependency.H2_DRIVER));
    try {
      Class<?> driverClass = classLoader.loadClass("org.h2.Driver");
      Method loadMethod = driverClass.getMethod("load");
      this.driver = (Driver) loadMethod.invoke(null);
    } catch (InvocationTargetException
        | NoSuchMethodException
        | IllegalAccessException
        | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void connect() throws SQLException {
    connection = driver.connect("jdbc:h2:" + file.toString(), new Properties());
  }

  @Override
  public Connection getConnection() throws SQLException {
    return connection;
  }

  @Override
  public void close() throws SQLException {
    connection.close();
  }
}
