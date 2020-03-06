package com.mrivanplays.simpleregister.commands;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.mrivanplays.simpleregister.SimpleRegister;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandLogin implements CommandExecutor {

  private SimpleRegister plugin;

  public CommandLogin(SimpleRegister plugin) {
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

              BCrypt.Result result =
                  BCrypt.verifyer()
                      .verify(password.toCharArray(), entry.getPassword().toCharArray());
              if (!result.verified) {
                player.sendMessage(plugin.getConfiguration().getString("messages.wrong_password"));
                return;
              }

              plugin.getSessionHandler().addLoggedIn(player.getUniqueId());
              player.sendMessage(plugin.getConfiguration().getString("messages.login_successful"));
            });
    return true;
  }
}
