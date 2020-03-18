package com.mrivanplays.simpleregister.commands;

import com.mrivanplays.simpleregister.SimpleRegister;
import com.mrivanplays.simpleregister.storage.PasswordEntry;
import com.mrivanplays.simpleregister.util.PasswordEncryptionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandChangePassword implements CommandExecutor {

  private SimpleRegister plugin;

  public CommandChangePassword(SimpleRegister plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("Nope.");
      return true;
    }
    Player player = (Player) sender;
    if (args.length < 2) {
      player.sendMessage(
          plugin.getConfiguration().getString("messages.invalid_changepassword_usage"));
      return true;
    }
    String oldPassword = args[0];
    String newPassword = args[1];

    plugin
        .getStorage()
        .getPasswordEntry(
            player.getUniqueId(),
            entry -> {
              if (!PasswordEncryptionUtil.verifyPasswords(oldPassword, entry.getPassword())) {
                player.sendMessage(
                    plugin.getConfiguration().getString("messages.invalid_current_password"));
                return;
              }

              plugin
                  .getStorage()
                  .modifyPassword(
                      player.getUniqueId(),
                      new PasswordEntry(
                          entry.getId(),
                          entry.getName(),
                          entry.getPlayerUUID(),
                          entry.getPlayerIP(),
                          PasswordEncryptionUtil.hash(newPassword)));

              player.sendMessage(plugin.getConfiguration().getString("messages.password_changed_successfully"));
            });
    return true;
  }
}
