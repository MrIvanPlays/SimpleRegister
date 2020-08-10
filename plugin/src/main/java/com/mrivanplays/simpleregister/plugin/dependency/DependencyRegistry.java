package com.mrivanplays.simpleregister.plugin.dependency;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.mrivanplays.simpleregister.plugin.storage.StorageType;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Credits: lucko Source: github.com/lucko/LuckPerms
 *
 * @author lucko
 */
public class DependencyRegistry {

  private static final ListMultimap<StorageType, Dependency> STORAGE_DEPENDENCIES =
      ImmutableListMultimap.<StorageType, Dependency>builder()
          .putAll(
              StorageType.MYSQL,
              Dependency.MYSQL_DRIVER,
              Dependency.SLF4J_API,
              Dependency.SLF4J_SIMPLE,
              Dependency.HIKARI)
          .putAll(
              StorageType.MARIADB,
              Dependency.MARIADB_DRIVER,
              Dependency.SLF4J_API,
              Dependency.SLF4J_SIMPLE,
              Dependency.HIKARI)
          .putAll(
              StorageType.POSTGRESQL,
              Dependency.POSTGRESQL_DRIVER,
              Dependency.SLF4J_API,
              Dependency.SLF4J_SIMPLE,
              Dependency.HIKARI)
          .putAll(StorageType.H2, Dependency.H2_DRIVER)
          .putAll(StorageType.SQLITE, Dependency.SQLITE_DRIVER)
          .build();

  public Set<Dependency> resolveStorageDependencies(Set<StorageType> storageTypes) {
    Set<Dependency> dependencies = new LinkedHashSet<>();
    for (StorageType storageType : storageTypes) {
      dependencies.addAll(STORAGE_DEPENDENCIES.get(storageType));
    }

    if ((dependencies.contains(Dependency.SLF4J_API))
        && (dependencies.contains(Dependency.SLF4J_SIMPLE))
        && slf4jPresent()) {
      dependencies.remove(Dependency.SLF4J_API);
      dependencies.remove(Dependency.SLF4J_SIMPLE);
    }

    return dependencies;
  }

  private static boolean classExists(String className) {
    try {
      Class.forName(className);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private static boolean slf4jPresent() {
    return classExists("org.slf4j.Logger") && classExists("org.slf4j.LoggerFactory");
  }

  public static boolean shouldAutoLoad(Dependency dependency) {
    switch (dependency) {
      case ASM:
      case ASM_COMMONS:
      case JAR_RELOCATOR:
      case H2_DRIVER:
      case SQLITE_DRIVER:
        return false;
      default:
        return true;
    }
  }
}
