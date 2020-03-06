package com.mrivanplays.simpleregister.listeners;

import com.mrivanplays.simpleregister.SimpleRegister;
import com.mrivanplays.simpleregister.storage.PasswordEntry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

public class PluginEventListener implements Listener {

  private SimpleRegister plugin;

  public PluginEventListener(SimpleRegister plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    if (plugin.getConfiguration().getBoolean("forceSpawnTeleport")
        && plugin.getSpawn().getLocation() != null) {
      event.getPlayer().teleport(plugin.getSpawn().getLocation());
    }
    new Runnable() {

      final BukkitTask task =
          plugin
              .getServer()
              .getScheduler()
              .runTaskTimer(
                  plugin, this, 0, plugin.getConfiguration().getInt("spam_each_seconds") * 20);
      int secondsPassed;

      @Override
      public void run() {
        if (plugin.getSessionHandler().hasLoggedIn(event.getPlayer().getUniqueId())) {
          task.cancel();
          return;
        }
        PasswordEntry entry = plugin.getStorage().getPasswordEntry(event.getPlayer().getUniqueId());
        if (entry == null) {
          event
              .getPlayer()
              .sendMessage(plugin.getConfiguration().getString("messages.register_message"));
        } else {
          event
              .getPlayer()
              .sendMessage(plugin.getConfiguration().getString("messages.login_message"));
        }
        int delay = plugin.getConfiguration().getInt("spam_each_seconds");
        secondsPassed = secondsPassed + delay;
        if (secondsPassed == plugin.getConfiguration().getInt("kick_at_seconds")) {
          task.cancel();
          event
              .getPlayer()
              .kickPlayer(plugin.getConfiguration().getString("messages.time_exceeded"));
        }
      }
    };
  }

  @EventHandler
  public void onPreProcess(PlayerCommandPreprocessEvent event) {
    String commandName = event.getMessage().split(" ")[0].replace("/", "");
    if (!commandName.equalsIgnoreCase("login")
        || !commandName.equalsIgnoreCase("l")
        || !commandName.equalsIgnoreCase("register")) {
      if (plugin.getSessionHandler().hasLoggedIn(event.getPlayer().getUniqueId())) {
        return;
      }
      if (!plugin.getConfiguration().getBoolean("allowOtherCommands")) {
        event.setCancelled(true);
        event
            .getPlayer()
            .sendMessage(plugin.getConfiguration().getString(getMessageKey(commandName)));
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
    if (plugin.getSessionHandler().hasLoggedIn(event.getPlayer().getUniqueId())) {
      return;
    }
    if (!plugin.getConfiguration().getBoolean("allowMovement")) {
      event.setCancelled(true);
      event
          .getPlayer()
          .sendMessage(plugin.getConfiguration().getString("messages.have_to_be_logged_in"));
    }
  }

  @EventHandler
  public void onChat(AsyncPlayerChatEvent event) {
    if (plugin.getSessionHandler().hasLoggedIn(event.getPlayer().getUniqueId())) {
      return;
    }
    if (!plugin.getConfiguration().getBoolean("allowChat")) {
      event.setCancelled(true);
      event
          .getPlayer()
          .sendMessage(plugin.getConfiguration().getString("messages.have_to_be_logged_in"));
    }
  }

  @EventHandler
  public void onClick(InventoryClickEvent event) {
    if (plugin.getSessionHandler().hasLoggedIn(event.getWhoClicked().getUniqueId())) {
      return;
    }
    if (!plugin.getConfiguration().getBoolean("allowInventoryInteraction")) {
      event.setCancelled(true);
      event.getWhoClicked().closeInventory();
      event
          .getWhoClicked()
          .sendMessage(plugin.getConfiguration().getString("messages.have_to_be_logged_in"));
    }
  }

  @EventHandler
  public void onDrop(PlayerDropItemEvent event) {
    if (plugin.getSessionHandler().hasLoggedIn(event.getPlayer().getUniqueId())) {
      return;
    }
    if (!plugin.getConfiguration().getBoolean("allowItemDrop")) {
      event.setCancelled(true);
      event.getPlayer().sendMessage(plugin.getConfiguration().getString("message.have_to_be_logged_in"));
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    plugin.getSessionHandler().onQuit(event.getPlayer().getUniqueId());
  }
}
