package com.mrivanplays.simpleregister;

import com.mrivanplays.simpleregister.commands.CommandLogin;
import com.mrivanplays.simpleregister.commands.CommandRegister;
import com.mrivanplays.simpleregister.commands.CommandUnregister;
import com.mrivanplays.simpleregister.commands.SimpleRegisterCommands;
import com.mrivanplays.simpleregister.config.Configuration;
import com.mrivanplays.simpleregister.listeners.PluginEventListener;
import com.mrivanplays.simpleregister.storage.SpawnYAML;
import com.mrivanplays.simpleregister.storage.Storage;
import com.mrivanplays.simpleregister.util.Log4jFiltering;
import java.util.Collections;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleRegister extends JavaPlugin {

  private Storage storage;
  private Configuration config;
  private SpawnYAML spawn;
  private PlayerSessionHandler sessionHandler;

  @Override
  public void onEnable() {
    Log4jFiltering.setup();
    long start = System.currentTimeMillis();
    storage = new Storage(getDataFolder());
    config = new Configuration(getDataFolder());
    spawn = new SpawnYAML(getDataFolder());
    sessionHandler = new PlayerSessionHandler();

    registerCommands();

    getServer().getPluginManager().registerEvents(new PluginEventListener(this), this);

    long end = System.currentTimeMillis() - start;
    getLogger().info("Enabled! Took " + end + " ms");
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
}
