package com.mrivanplays.simpleregister.commands;

import com.mrivanplays.simpleregister.SimpleRegister;
import com.mrivanplays.simpleregister.storage.PasswordEntry;
import com.mrivanplays.simpleregister.util.PasswordEncryptionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandRegister implements CommandExecutor {

  private SimpleRegister plugin;

  public CommandRegister(SimpleRegister plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("Player only");
      return true;
    }
    Player player = (Player) sender;

    plugin
        .getStorage()
        .getPasswordEntry(
            player.getUniqueId(),
            entry -> {
              if (entry != null) {
                player.sendMessage(
                    plugin.getConfiguration().getString("messages.already_registered"));
                return;
              }
              String ip = player.getAddress().getAddress().getHostName();
              plugin
                  .getStorage()
                  .getAltAccounts(
                      ip,
                      accounts -> {
                        if (accounts.size()
                            == plugin.getConfiguration().getInt("max_accounts_allowed")) {
                          player.kickPlayer(
                              plugin
                                  .getConfiguration()
                                  .getString("messages.max_accounts_exceeded"));
                          return;
                        }

                        if (args.length < 2) {
                          player.sendMessage(
                              plugin
                                  .getConfiguration()
                                  .getString("messages.invalid_register_usage"));
                          return;
                        }

                        String password = args[0];
                        String confirmationPassword = args[1];
                        if (!confirmationPassword.equalsIgnoreCase(password)) {
                          player.sendMessage(
                              plugin.getConfiguration().getString("messages.invalid_confirmation"));
                          return;
                        }

                        String hashedPassword = PasswordEncryptionUtil.hash(password);
                        PasswordEntry newEntry =
                            new PasswordEntry(
                                0, // doesn't matter as it is auto increment in sql and it is not
                                   // used in flatfile
                                player.getName(),
                                player.getUniqueId(),
                                ip,
                                hashedPassword);
                        plugin.getStorage().addPassword(newEntry);

                        plugin.getSessionHandler().addLoggedIn(player.getUniqueId());
                        player.sendMessage(
                            plugin.getConfiguration().getString("messages.register_successful"));
                      });
            });
    return true;
  }
}
