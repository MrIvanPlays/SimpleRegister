package com.mrivanplays.simpleregister.dependency.classloader;

import java.nio.file.Path;

/**
 * Credits: lucko
 * Source: github.com/lucko/LuckPerms
 *
 * @author lucko
 */
public interface PluginClassLoader {

  void addJarToClasspath(Path file);
}
