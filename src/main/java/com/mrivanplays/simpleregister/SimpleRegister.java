package com.mrivanplays.simpleregister;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.mrivanplays.simpleregister.commands.CommandChangePassword;
import com.mrivanplays.simpleregister.commands.CommandLogin;
import com.mrivanplays.simpleregister.commands.CommandRegister;
import com.mrivanplays.simpleregister.commands.CommandUnregister;
import com.mrivanplays.simpleregister.commands.SimpleRegisterCommands;
import com.mrivanplays.simpleregister.config.Configuration;
import com.mrivanplays.simpleregister.dependency.DependencyManager;
import com.mrivanplays.simpleregister.dependency.classloader.PluginClassLoader;
import com.mrivanplays.simpleregister.dependency.classloader.ReflectionClassLoader;
import com.mrivanplays.simpleregister.listeners.PluginEventListener;
import com.mrivanplays.simpleregister.storage.SpawnYAML;
import com.mrivanplays.simpleregister.storage.Storage;
import com.mrivanplays.simpleregister.storage.StorageType;
import com.mrivanplays.simpleregister.util.Log4jFiltering;
import java.util.Collections;
import java.util.EnumSet;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleRegister extends JavaPlugin {

  private Storage storage;
  private Configuration config;
  private SpawnYAML spawn;
  private PlayerSessionHandler sessionHandler;
  private PluginClassLoader classLoader;
  private DependencyManager dependencyManager;

  @Override
  public void onLoad() {
    config = new Configuration(getDataFolder());
    classLoader = new ReflectionClassLoader(this);
    dependencyManager = new DependencyManager(this);
    dependencyManager.loadStorageDependencies(
        EnumSet.of(StorageType.valueOf(config.getString("database.type").toUpperCase())));
  }

  private TaskChainFactory taskChainFactory;

  @Override
  public void onEnable() {
    Log4jFiltering.setup();
    taskChainFactory = BukkitTaskChainFactory.create(this);
    long start = System.currentTimeMillis();
    storage = new Storage(this);
    storage.connect();
    spawn = new SpawnYAML(getDataFolder());
    sessionHandler = new PlayerSessionHandler();

    registerCommands();

    getServer().getPluginManager().registerEvents(new PluginEventListener(this), this);

    long end = System.currentTimeMillis() - start;
    getLogger().info("Enabled! Took " + end + " ms");
  }

  @Override
  public void onDisable() {
    storage.close();
  }

  private void registerCommands() {
    // I hate bukkit's commands api.
    SimpleRegisterCommands commands = new SimpleRegisterCommands(this);
    getCommand("simpleregister").setExecutor(commands);
    getCommand("simpleregister").setTabCompleter(commands);

    TabCompleter EMPTY = (sender, command, label, args) -> Collections.emptyList();

    getCommand("login").setExecutor(new CommandLogin(this));
    getCommand("login").setTabCompleter(EMPTY);
    getCommand("register").setExecutor(new CommandRegister(this));
    getCommand("register").setTabCompleter(EMPTY);
    getCommand("unregister").setExecutor(new CommandUnregister(this));
    getCommand("unregister").setTabCompleter(EMPTY);
    getCommand("changepassword").setExecutor(new CommandChangePassword(this));
    getCommand("changepassword").setTabCompleter(EMPTY);
  }

  public Storage getStorage() {
    return storage;
  }

  public Configuration getConfiguration() {
    return config;
  }

  public SpawnYAML getSpawn() {
    return spawn;
  }

  public PlayerSessionHandler getSessionHandler() {
    return sessionHandler;
  }

  public PluginClassLoader getPluginClassLoader() {
    return classLoader;
  }

  public DependencyManager getDependencyManager() {
    return dependencyManager;
  }

  public <T> TaskChain<T> newChain() {
    return taskChainFactory.newChain();
  }

  public <T> TaskChain<T> newSharedChain(String name) {
    return taskChainFactory.newSharedChain(name);
  }
}
