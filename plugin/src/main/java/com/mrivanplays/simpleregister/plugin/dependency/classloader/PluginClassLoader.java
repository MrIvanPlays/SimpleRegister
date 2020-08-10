package com.mrivanplays.simpleregister.plugin.dependency.classloader;

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
