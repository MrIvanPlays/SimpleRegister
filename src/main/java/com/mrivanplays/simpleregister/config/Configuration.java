package com.mrivanplays.simpleregister.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Configuration {

  private File file;
  private FileConfiguration configuration;

  public Configuration(File dataFolder) {
    this.file = new File(dataFolder, "config.yml");
    createFileIfExists();
    configuration = YamlConfiguration.loadConfiguration(file);
  }

  private void createFileIfExists() {
    if (!file.exists()) {
      if (!file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }
      try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
        Files.copy(in, file.getAbsoluteFile().toPath());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public String getString(String key) {
    if (key.startsWith("messages")) {
      return ChatColor.translateAlternateColorCodes('&', configuration.getString(key));
    }
    return configuration.getString(key);
  }

  public boolean getBoolean(String key) {
    return configuration.getBoolean(key);
  }

  public int getInt(String key) {
    return configuration.getInt(key);
  }
}
