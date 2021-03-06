package com.mrivanplays.simpleregister.plugin.dependency.classloader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Credits: lucko
 * Source: github.com/lucko/LuckPerms
 *
 * @author lucko
 */
public class ReflectionClassLoader implements PluginClassLoader {
  private static final Method ADD_URL_METHOD;

  static {
    try {
      ADD_URL_METHOD = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      ADD_URL_METHOD.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private final URLClassLoader classLoader;

  public ReflectionClassLoader(JavaPlugin plugin) throws IllegalStateException {
    ClassLoader classLoader = plugin.getClass().getClassLoader();
    if (classLoader instanceof URLClassLoader) {
      this.classLoader = (URLClassLoader) classLoader;
    } else {
      throw new IllegalStateException("ClassLoader is not instance of URLClassLoader");
    }
  }

  @Override
  public void addJarToClasspath(Path file) {
    try {
      ADD_URL_METHOD.invoke(this.classLoader, file.toUri().toURL());
    } catch (IllegalAccessException | InvocationTargetException | MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
