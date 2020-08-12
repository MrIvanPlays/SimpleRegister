package com.mrivanplays.simpleregister.plugin.listeners;

import com.mrivanplays.simpleregister.plugin.SimpleRegisterPlugin;
import com.mrivanplays.simpleregister.plugin.TaskRegistry;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

// todo: Ivan, have you heard of single responcibility principle ????
public class PluginEventListener implements Listener {

  private final SimpleRegisterPlugin plugin;

  public PluginEventListener(SimpleRegisterPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    Location oldLocation = player.getLocation();
    if (plugin.getConfiguration().getBoolean("forceSpawnTeleport")
        && plugin.getSpawn().getLocation() != null) {
      if (plugin.getConfig().getBoolean("syncTeleportation")) {
        player.teleport(plugin.getSpawn().getLocation());
      } else {
        PaperLib.teleportAsync(player, plugin.getSpawn().getLocation());
      }
    }
    Runnable r =
        new Runnable() {

          int secondsPassed;

          @Override
          public void run() {
            plugin
                .getStorage()
                .getPasswordEntry(
                    player.getUniqueId(),
                    entry -> {
                      if (entry == null) {
                        player.sendMessage(
                            plugin.getConfiguration().getString("messages.register_message"));
                      } else {
                        player.sendMessage(
                            plugin.getConfiguration().getString("messages.login_message"));
                      }
                      int delay = plugin.getConfiguration().getInt("spam_each_seconds");
                      secondsPassed = secondsPassed + delay;
                      if (secondsPassed == plugin.getConfiguration().getInt("kick_at_seconds")) {
                        plugin
                            .getServer()
                            .getScheduler()
                            .runTask(
                                plugin,
                                () -> {
                                  if (plugin.getConfig().getBoolean("syncTeleportation")) {
                                    player.teleport(oldLocation);
                                  } else {
                                    PaperLib.teleportAsync(player, oldLocation);
                                  }
                                });
                        plugin
                            .getServer()
                            .getScheduler()
                            .runTask(
                                plugin,
                                () ->
                                    player.kickPlayer(
                                        plugin
                                            .getConfiguration()
                                            .getString("messages.time_exceeded")));
                      }
                    });
          }
        };
    BukkitTask task =
        plugin
            .getServer()
            .getScheduler()
            .runTaskTimerAsynchronously(
                plugin, r, 0, plugin.getConfiguration().getInt("spam_each_seconds") * 20);
    TaskRegistry.putTask(player.getUniqueId(), task);
  }

  @EventHandler
  public void onPreProcess(PlayerCommandPreprocessEvent event) {
    Player player = event.getPlayer();
    String commandName = event.getMessage().split(" ")[0].replace("/", "");
    if (!commandName.equalsIgnoreCase("login")
        && !commandName.equalsIgnoreCase("l")
        && !commandName.equalsIgnoreCase("register")) {
      if (plugin.getSessionHandler().hasLoggedIn(player.getUniqueId())) {
        return;
      }
      if (!plugin.getConfiguration().getBoolean("allowOtherCommands")) {
        event.setCancelled(true);
        player.sendMessage(plugin.getConfiguration().getString(getMessageKey(commandName)));
      }
    }
  }

  private String getMessageKey(String commandName) {
    return commandName.equalsIgnoreCase("login")
        ? "messages.have_to_login"
        : "messages.have_to_register";
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    if (plugin.getSessionHandler().hasLoggedIn(player.getUniqueId())) {
      return;
    }
    if (!plugin.getConfiguration().getBoolean("allowMovement")) {
      event.setCancelled(true);
      if (plugin.getConfiguration().getBoolean("send_message_while_trying_move")) {
        player.sendMessage(plugin.getConfiguration().getString("messages.have_to_be_logged_in"));
      }
    }
  }

  @EventHandler
  public void onChat(AsyncPlayerChatEvent event) {
    Player player = event.getPlayer();
    if (plugin.getSessionHandler().hasLoggedIn(event.getPlayer().getUniqueId())) {
      return;
    }
    if (!plugin.getConfiguration().getBoolean("allowChat")) {
      event.setCancelled(true);
      player.sendMessage(plugin.getConfiguration().getString("messages.have_to_be_logged_in"));
    }
  }

  @EventHandler
  public void onClick(InventoryClickEvent event) {
    // too lazy to cast
    HumanEntity player = event.getWhoClicked();
    if (plugin.getSessionHandler().hasLoggedIn(player.getUniqueId())) {
      return;
    }
    if (!plugin.getConfiguration().getBoolean("allowInventoryInteraction")) {
      event.setCancelled(true);
      player.closeInventory();
      player.sendMessage(plugin.getConfiguration().getString("messages.have_to_be_logged_in"));
    }
  }

  @EventHandler
  public void onDrop(PlayerDropItemEvent event) {
    Player player = event.getPlayer();
    if (plugin.getSessionHandler().hasLoggedIn(player.getUniqueId())) {
      return;
    }
    if (!plugin.getConfiguration().getBoolean("allowItemDrop")) {
      event.setCancelled(true);
      player.sendMessage(plugin.getConfiguration().getString("messages.have_to_be_logged_in"));
    }
  }

  @EventHandler
  public void onKick(PlayerKickEvent event) {
    plugin.getSessionHandler().onQuit(event.getPlayer().getUniqueId());
    TaskRegistry.cancelTask(event.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    plugin.getSessionHandler().onQuit(event.getPlayer().getUniqueId());
    TaskRegistry.cancelTask(event.getPlayer().getUniqueId());
  }
}
