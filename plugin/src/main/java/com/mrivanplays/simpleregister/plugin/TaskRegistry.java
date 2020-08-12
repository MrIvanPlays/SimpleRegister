package com.mrivanplays.simpleregister.plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.scheduler.BukkitTask;

public class TaskRegistry {

  private static Map<UUID, BukkitTask> TASKS_BY_PLAYER = new ConcurrentHashMap<>();

  public static void cancelTask(UUID uuid) {
    if (TASKS_BY_PLAYER.get(uuid) != null) {
      TASKS_BY_PLAYER.remove(uuid).cancel();
    }
  }

  public static void putTask(UUID uuid, BukkitTask task) {
    TASKS_BY_PLAYER.put(uuid, task);
  }
}
