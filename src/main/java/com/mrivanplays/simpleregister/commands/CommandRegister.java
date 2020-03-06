package com.mrivanplays.simpleregister.commands;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.mrivanplays.simpleregister.SimpleRegister;
import com.mrivanplays.simpleregister.storage.PasswordEntry;
import java.util.List;
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

    if (plugin.getStorage().getPasswordEntry(player.getUniqueId()) != null) {
      player.sendMessage(plugin.getConfiguration().getString("messages.already_registered"));
      return true;
    }
    String ip = player.getAddress().getAddress().getHostName();
    List<PasswordEntry> accounts = plugin.getStorage().getAltAccounts(ip);
    if (accounts.size() == plugin.getConfiguration().getInt("max_accounts_allowed")) {
      player.kickPlayer(plugin.getConfiguration().getString("messages.max_accounts_exceeded"));
      return true;
    }

    if (args.length < 2) {
      player.sendMessage(plugin.getConfiguration().getString("messages.invalid_register_usage"));
      return true;
    }

    String password = args[0];
    String confirmationPassword = args[1];
    if (!confirmationPassword.equalsIgnoreCase(password)) {
      player.sendMessage(plugin.getConfiguration().getString("messages.invalid_confirmation"));
      return true;
    }

    String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
    PasswordEntry entry = new PasswordEntry(player.getUniqueId(), ip, hashedPassword);
    plugin.getStorage().addPassword(entry);

    plugin.getSessionHandler().addLoggedIn(player.getUniqueId());
    player.sendMessage(plugin.getConfiguration().getString("messages.register_successful"));
    return true;
  }
}
