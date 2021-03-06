package com.mrivanplays.simpleregister.plugin.commands;

import com.mrivanplays.simpleregister.plugin.SimpleRegisterPlugin;
import com.mrivanplays.simpleregister.plugin.storage.PasswordEntry;
import com.mrivanplays.simpleregister.plugin.util.PasswordEncryptionUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class SimpleRegisterCommands implements TabExecutor {

  private SimpleRegisterPlugin plugin;

  public SimpleRegisterCommands(SimpleRegisterPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!sender.hasPermission("simpleregister.admin")) {
      sender.sendMessage(plugin.getConfiguration().getString("messages.insufficient_permissions"));
      return true;
    }
    if (args.length == 0) {
      sender.sendMessage(
          "/simpleregister forceregister <playerName> <password> - Forcibly registers the specified player");
      sender.sendMessage(
          "/simpleregister changepassword <playerName> <password> - Forcibly changes the password of the specified player");
      sender.sendMessage(
          "/simpleregister viewalts <playerName> - Lists the alt accounts the player have registered");
      sender.sendMessage(
          "/simpleregister unregister <playerName> - Forcibly unregisters the specified player");
      sender.sendMessage(
          "/simpleregister setspawn - Sets the point where all players will be forcibly teleported when they join the server.");
      sender.sendMessage("/simpleregister info - Gives information about the plugin");
      return true;
    }

    switch (args[0]) {
      case "forceregister":
        if (args.length < 3) {
          sender.sendMessage("Usage: /simpleregister forceregister <playerName> <password>");
          return true;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
        String password = args[2];
        plugin
            .getStorage()
            .addPassword(
                new PasswordEntry(
                    0, // as I said in CommandRegister - doesn't matter
                    args[1], player.getUniqueId(), null, PasswordEncryptionUtil.hash(password)));
        sender.sendMessage("Player registered successfully");
        break;
      case "changepassword":
        if (args.length < 3) {
          sender.sendMessage("Usage: /simpleregister changepassword <playerName> <password>");
          return true;
        }

        OfflinePlayer cpPlayer = Bukkit.getOfflinePlayer(args[1]);
        plugin
            .getStorage()
            .getPasswordEntry(
                cpPlayer.getUniqueId(),
                entry -> {
                  if (entry == null) {
                    sender.sendMessage("Cannot change a password of unregistered user");
                    return;
                  }

                  String cpPassword = args[2];

                  PasswordEntry newEntry =
                      new PasswordEntry(
                          entry.getId(),
                          args[1],
                          cpPlayer.getUniqueId(),
                          entry.getPlayerIP(),
                          PasswordEncryptionUtil.hash(cpPassword));

                  plugin.getStorage().modifyPassword(cpPlayer.getUniqueId(), newEntry);
                  sender.sendMessage("Password changed successfully");
                });
        break;
      case "viewalts":
        if (args.length < 2) {
          sender.sendMessage("Usage: /simpleregister viewalts <playerName>");
          return true;
        }

        OfflinePlayer vaPlayer = Bukkit.getOfflinePlayer(args[1]);
        plugin
            .getStorage()
            .getPasswordEntry(
                vaPlayer.getUniqueId(),
                passwordEntry -> {
                  if (passwordEntry == null) {
                    sender.sendMessage(
                        "The specified player isn't registered. We cannot lookup for alts.");
                    return;
                  }
                  if (passwordEntry.getPlayerIP() == null) {
                    sender.sendMessage(
                        "The specified player was forcibly registered. We cannot lookup for alts.");
                    return;
                  }
                  plugin
                      .getStorage()
                      .getAltAccounts(
                          passwordEntry.getPlayerIP(),
                          alts -> {
                            alts.removeIf(e -> e.getPlayerUUID().equals(vaPlayer.getUniqueId()));

                            if (alts.isEmpty()) {
                              sender.sendMessage("Player " + args[1] + " has no alt accounts.");
                              return;
                            }

                            sender.sendMessage(
                                "Player " + args[1] + " has " + alts.size() + " alt accounts");
                            StringBuilder builder = new StringBuilder();
                            for (PasswordEntry altEntry : alts) {
                              OfflinePlayer alt = Bukkit.getOfflinePlayer(altEntry.getPlayerUUID());
                              builder.append(alt.getName()).append(" ; ");
                            }
                            sender.sendMessage(builder.substring(0, builder.length() - 3));
                          });
                });
        break;
      case "unregister":
        if (args.length < 2) {
          sender.sendMessage("Usage: /simpleregister unregister <playerName>");
          return true;
        }

        OfflinePlayer uPlayer = Bukkit.getOfflinePlayer(args[1]);
        plugin
            .getStorage()
            .getPasswordEntry(
                uPlayer.getUniqueId(),
                pEntry -> {
                  if (pEntry == null) {
                    sender.sendMessage("Cannot unregister a non-registered user.");
                    return;
                  }

                  plugin.getStorage().removeEntry(uPlayer.getUniqueId());
                  sender.sendMessage("Player unregistered successfully");
                });
        break;
      case "setspawn":
        if (!(sender instanceof Player)) {
          sender.sendMessage("Player only");
          return true;
        }
        Player executor = (Player) sender;
        plugin.getSpawn().setLocation(executor.getLocation());
        executor.sendMessage("Spawn location set.");
        break;
      case "info":
        String implementationVersion = plugin.getClass().getPackage().getImplementationVersion();
        String pluginVersion = plugin.getDescription().getVersion();
        String name = plugin.getDescription().getName();
        String description = plugin.getDescription().getDescription();
        String authors = String.join(", ", plugin.getDescription().getAuthors());
        sender.sendMessage(
            new String[] {
              "Name: " + name + ";",
              "Description: " + description + ";",
              "Authors: " + authors + ";",
              "Plugin version: " + pluginVersion + ";",
              "Exact version: " + implementationVersion
            });
        break;
      default:
        sender.sendMessage("Unknown command. Run \"/simpleregister\" for a list of commands.");
    }
    return true;
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String alias, String[] args) {
    if (args.length == 1) {
      if (!sender.hasPermission("simpleregister.admin")) {
        return Collections.emptyList();
      }
      return Arrays.stream(
              new String[] {
                "forceregister", "changepassword", "viewalts", "unregister", "setspawn", "info"
              })
          .filter(entry -> entry.startsWith(args[0].toLowerCase()))
          .collect(Collectors.toList());
    }
    return null;
  }
}
