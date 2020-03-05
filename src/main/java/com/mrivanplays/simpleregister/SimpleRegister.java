package com.mrivanplays.simpleregister;

import com.mrivanplays.simpleregister.commands.SimpleRegisterCommands;
import com.mrivanplays.simpleregister.config.Configuration;
import com.mrivanplays.simpleregister.listeners.PluginEventListener;
import com.mrivanplays.simpleregister.storage.Storage;
import com.mrivanplays.simpleregister.util.Log4jFiltering;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleRegister extends JavaPlugin {

  private Storage storage;
  private Configuration config;
  private PlayerSessionHandler sessionHandler;

  @Override
  public void onEnable() {
    Log4jFiltering.setup();
    long start = System.currentTimeMillis();
    storage = new Storage(getDataFolder());
    config = new Configuration(getDataFolder());
    sessionHandler = new PlayerSessionHandler();

    // command register
    SimpleRegisterCommands commands = new SimpleRegisterCommands(this);
    getCommand("simpleregister").setExecutor(commands);
    getCommand("unregister").setExecutor(commands);
    getCommand("simpleregister").setTabCompleter(commands);
    getCommand("unregister").setTabCompleter(commands);
    getServer().getPluginManager().registerEvents(new PluginEventListener(this), this);

    long end = System.currentTimeMillis() - start;
    getLogger().info("Enabled! Took " + end + " ms");
  }

  public Storage getStorage() {
    return storage;
  }

  public Configuration getConfiguration() {
    return config;
  }

  public PlayerSessionHandler getSessionHandler() {
    return sessionHandler;
  }
}
