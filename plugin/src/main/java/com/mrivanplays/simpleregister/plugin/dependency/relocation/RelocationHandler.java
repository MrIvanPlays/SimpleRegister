package com.mrivanplays.simpleregister.plugin.dependency.relocation;

import com.mrivanplays.simpleregister.plugin.dependency.Dependency;
import com.mrivanplays.simpleregister.plugin.dependency.DependencyManager;
import com.mrivanplays.simpleregister.plugin.dependency.classloader.IsolatedClassLoader;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Credits: lucko
 * Source: github.com/lucko/LuckPerms
 *
 * @author lucko
 */
public class RelocationHandler {
  public static final Set<Dependency> DEPENDENCIES = EnumSet
      .of(Dependency.ASM, Dependency.ASM_COMMONS, Dependency.JAR_RELOCATOR);
  private static final String JAR_RELOCATOR_CLASS = "me.lucko.jarrelocator.JarRelocator";
  private static final String JAR_RELOCATOR_RUN_METHOD = "run";

  private final Constructor<?> jarRelocatorConstructor;
  private final Method jarRelocatorRunMethod;

  public RelocationHandler(DependencyManager dependencyManager) {
    try {
      dependencyManager.loadDependencies(DEPENDENCIES);
      IsolatedClassLoader classLoader = dependencyManager.obtainClassLoaderWith(DEPENDENCIES);

      Class<?> jarRelocatorClass = classLoader.loadClass(JAR_RELOCATOR_CLASS);

      this.jarRelocatorConstructor = jarRelocatorClass.getDeclaredConstructor(File.class, File.class, Map.class);
      this.jarRelocatorConstructor.setAccessible(true);

      this.jarRelocatorRunMethod = jarRelocatorClass.getDeclaredMethod(JAR_RELOCATOR_RUN_METHOD);
      this.jarRelocatorRunMethod.setAccessible(true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void remap(Path input, Path output, List<Relocation> relocations) throws Exception {
    Map<String, String> mappings = new HashMap<>();
    for (Relocation relocation : relocations) {
      mappings.put(relocation.getPattern(), relocation.getRelocatedPattern());
    }

    Object relocator = this.jarRelocatorConstructor.newInstance(input.toFile(), output.toFile(), mappings);
    this.jarRelocatorRunMethod.invoke(relocator);
  }
}
