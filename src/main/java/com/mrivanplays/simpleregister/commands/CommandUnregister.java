package com.mrivanplays.simpleregister.commands;

import com.mrivanplays.simpleregister.SimpleRegister;
import com.mrivanplays.simpleregister.util.PasswordEncryptionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUnregister implements CommandExecutor {

  private SimpleRegister plugin;

  public CommandUnregister(SimpleRegister plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (command.getName().equalsIgnoreCase("unregister")) {
      if (!(sender instanceof Player)) {
        sender.sendMessage("Player only");
        return true;
      }
      Player player = (Player) sender;
      if (args.length < 1) {
        player.sendMessage(
            plugin.getConfiguration().getString("messages.invalid_unregister_usage"));
        return true;
      }
      String password = args[0];
      plugin
          .getStorage()
          .getPasswordEntry(
              player.getUniqueId(),
              pwEntry -> {
                if (PasswordEncryptionUtil.verifyPasswords(password, pwEntry.getPassword())) {
                  plugin.getStorage().removeEntry(player.getUniqueId());

                  player.kickPlayer(
                      plugin.getConfiguration().getString("messages.unregister_successful"));
                } else {
                  player.sendMessage(
                      plugin.getConfiguration().getString("messages.wrong_password"));
                }
              });
    }
    return true;
  }
}
