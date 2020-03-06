package com.mrivanplays.simpleregister.dependency;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import com.mrivanplays.simpleregister.SimpleRegister;
import com.mrivanplays.simpleregister.dependency.classloader.IsolatedClassLoader;
import com.mrivanplays.simpleregister.dependency.classloader.PluginClassLoader;
import com.mrivanplays.simpleregister.dependency.relocation.Relocation;
import com.mrivanplays.simpleregister.dependency.relocation.RelocationHandler;
import com.mrivanplays.simpleregister.storage.StorageType;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Credits: lucko
 * Source: github.com/lucko/LuckPerms
 *
 * @author lucko
 */
public class DependencyManager {
  private final PluginClassLoader classLoader;
  private final Logger logger;
  private final MessageDigest digest;
  private final DependencyRegistry registry;
  private final File libsDirectory;

  private final EnumMap<Dependency, Path> loaded = new EnumMap<>(Dependency.class);
  private final Map<ImmutableSet<Dependency>, IsolatedClassLoader> loaders = new HashMap<>();
  private RelocationHandler relocationHandler = null;

  public DependencyManager(SimpleRegister plugin) {
    this.classLoader = plugin.getPluginClassLoader();
    this.logger = plugin.getLogger();
    try {
      this.digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    this.registry = new DependencyRegistry();

    this.libsDirectory = new File(plugin.getDataFolder(), "libs");
    if (!libsDirectory.exists()) {
      libsDirectory.mkdirs();
    }
  }

  private synchronized RelocationHandler getRelocationHandler() {
    if (this.relocationHandler == null) {
      this.relocationHandler = new RelocationHandler(this);
    }
    return this.relocationHandler;
  }

  public IsolatedClassLoader obtainClassLoaderWith(Set<Dependency> dependencies) {
    ImmutableSet<Dependency> set = ImmutableSet.copyOf(dependencies);

    for (Dependency dependency : dependencies) {
      if (!this.loaded.containsKey(dependency)) {
        throw new IllegalStateException("Dependency " + dependency + " is not loaded.");
      }
    }

    synchronized (this.loaders) {
      IsolatedClassLoader classLoader = this.loaders.get(set);
      if (classLoader != null) {
        return classLoader;
      }

      URL[] urls =
          set.stream()
              .map(this.loaded::get)
              .map(
                  file -> {
                    try {
                      return file.toUri().toURL();
                    } catch (MalformedURLException e) {
                      throw new RuntimeException(e);
                    }
                  })
              .toArray(URL[]::new);

      classLoader = new IsolatedClassLoader(urls);
      this.loaders.put(set, classLoader);
      return classLoader;
    }
  }

  public void loadStorageDependencies(Set<StorageType> storageTypes) {
    loadDependencies(this.registry.resolveStorageDependencies(storageTypes));
  }

  public void loadDependencies(Set<Dependency> dependencies) {
    List<Source> sources = new ArrayList<>();

    for (Dependency dependency : dependencies) {
      if (this.loaded.containsKey(dependency)) {
        continue;
      }

      try {
        Path file = downloadDependency(dependency);
        sources.add(new Source(dependency, file));
      } catch (Throwable e) {
        this.logger.severe("Exception whilst downloading dependency " + dependency.name());
        e.printStackTrace();
      }
    }

    List<Source> remappedJars = new ArrayList<>(sources.size());
    for (Source source : sources) {
      try {
        List<Relocation> relocations = new ArrayList<>(source.dependency.getRelocations());

        if (relocations.isEmpty()) {
          remappedJars.add(source);
          continue;
        }

        Path input = source.file;
        Path output =
            new File(libsDirectory, source.dependency.getFileName() + "-remapped.jar").toPath();

        if (Files.exists(output)) {
          remappedJars.add(new Source(source.dependency, output));
          continue;
        }

        RelocationHandler relocationHandler = getRelocationHandler();
        relocationHandler.remap(input, output, relocations);

        remappedJars.add(new Source(source.dependency, output));
      } catch (Throwable e) {
        logger.severe("Unable to remap the source file '" + source.dependency.name() + "'.");
        e.printStackTrace();
      }
    }

    for (Source source : remappedJars) {
      if (!DependencyRegistry.shouldAutoLoad(source.dependency)) {
        this.loaded.put(source.dependency, source.file);
        continue;
      }

      try {
        classLoader.addJarToClasspath(source.file);
        this.loaded.put(source.dependency, source.file);
      } catch (Throwable e) {
        logger.severe(
            "Failed to load dependency jar '" + source.file.getFileName().toString() + "'.");
        e.printStackTrace();
      }
    }
  }

  private Path downloadDependency(Dependency dependency) {
    Path file = new File(libsDirectory, dependency.getFileName() + ".jar").toPath();

    if (Files.exists(file)) {
      return file;
    }

    boolean success = false;
    Exception lastError = null;

    List<URL> urls = dependency.getUrls();
    for (int i = 0; i < urls.size() && !success; i++) {
      URL url = urls.get(i);

      try {
        URLConnection connection = url.openConnection();

        if (i == 0) {
          connection.setRequestProperty("User-Agent", "networkbans");
          connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5));
          connection.setReadTimeout((int) TimeUnit.SECONDS.toMillis(10));
        }

        try (InputStream in = connection.getInputStream()) {
          byte[] bytes = ByteStreams.toByteArray(in);
          if (bytes.length == 0) {
            throw new RuntimeException("Empty stream");
          }

          byte[] hash = this.digest.digest(bytes);

          if (!Arrays.equals(hash, dependency.getChecksum())) {
            throw new RuntimeException(
                "Downloaded file had an invalid hash. "
                    + "Expected: "
                    + Base64.getEncoder().encodeToString(dependency.getChecksum())
                    + " "
                    + "Actual: "
                    + Base64.getEncoder().encodeToString(hash));
          }

          Files.write(file, bytes);
          success = true;
        }
      } catch (Exception e) {
        lastError = e;
      }
    }

    if (!success) {
      throw new RuntimeException("Unable to download", lastError);
    }

    if (!Files.exists(file)) {
      throw new IllegalStateException("File not present: " + file.toString());
    } else {
      return file;
    }
  }

  private static final class Source {
    private final Dependency dependency;
    private final Path file;

    private Source(Dependency dependency, Path file) {
      this.dependency = dependency;
      this.file = file;
    }
  }
}
