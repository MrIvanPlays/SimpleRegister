package com.mrivanplays.simpleregister.plugin.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mrivanplays.simpleregister.plugin.SimpleRegisterPlugin;
import com.mrivanplays.simpleregister.plugin.util.PasswordEncryptionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandLogin implements CommandExecutor {

  private SimpleRegisterPlugin plugin;

  public CommandLogin(SimpleRegisterPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("Player only");
      return true;
    }
    Player player = (Player) sender;
    if (plugin.getSessionHandler().hasLoggedIn(player.getUniqueId())) {
      player.sendMessage(plugin.getConfiguration().getString("messages.already_logged_in"));
      return true;
    }

    if (args.length < 1) {
      player.sendMessage(plugin.getConfiguration().getString("messages.invalid_login_usage"));
      return true;
    }

    String password = args[0];
    plugin
        .getStorage()
        .getPasswordEntry(
            player.getUniqueId(),
            entry -> {
              if (entry == null) {
                player.sendMessage(
                    plugin.getConfiguration().getString("messages.have_to_register_first"));
                return;
              }

              if (!PasswordEncryptionUtil.verifyPasswords(password, entry.getPassword())) {
                player.sendMessage(plugin.getConfiguration().getString("messages.wrong_password"));
                return;
              }

              plugin
                  .getServer()
                  .getScheduler()
                  .runTask(
                      plugin,
                      () -> {
                        plugin.getSessionHandler().addLoggedIn(player.getUniqueId());
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("LoggedIn");
                        out.writeUTF(player.getUniqueId().toString());
                        player.sendPluginMessage(
                            plugin, "simpleregister:plugin", out.toByteArray());
                      });
              player.sendMessage(plugin.getConfiguration().getString("messages.login_successful"));
            });
    return true;
  }
}
