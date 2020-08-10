package com.mrivanplays.simpleregister.plugin.storage;

import java.io.File;
import java.io.IOException;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SpawnYAML {

  private File file;
  private FileConfiguration config;

  public SpawnYAML(File dataFolder) {
    this.file = new File(dataFolder, "spawn.yml");
    if (!file.exists()) {
      if (!file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    config = YamlConfiguration.loadConfiguration(file);
  }

  public Location getLocation() {
    return (Location) config.get("spawn");
  }

  public void setLocation(Location location) {
    config.set("spawn", location);
    try {
      config.save(file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
