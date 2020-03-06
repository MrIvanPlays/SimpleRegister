package com.mrivanplays.simpleregister.dependency.classloader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Credits: lucko
 * Source: github.com/lucko/LuckPerms
 *
 * @author lucko
 */
public class IsolatedClassLoader extends URLClassLoader {

  static {
    ClassLoader.registerAsParallelCapable();
  }

  public IsolatedClassLoader(URL[] urls) {
    super(urls, ClassLoader.getSystemClassLoader().getParent());
  }
}
