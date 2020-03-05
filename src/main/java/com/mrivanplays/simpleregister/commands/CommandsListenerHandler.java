package com.mrivanplays.simpleregister.commands;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.mrivanplays.simpleregister.SimpleRegister;
import com.mrivanplays.simpleregister.storage.PasswordEntry;
import java.util.List;
import org.bukkit.entity.Player;

public class CommandsListenerHandler {

  public static void handleLogin(SimpleRegister plugin, Player player, String[] args) {
    if (plugin.getSessionHandler().hasLoggedIn(player.getUniqueId())) {
      player.sendMessage(plugin.getConfiguration().getString("messages.already_logged_in"));
      return;
    }

    if (args.length < 1) {
      player.sendMessage(plugin.getConfiguration().getString("messages.invalid_login_usage"));
      return;
    }

    String password = args[0];
    PasswordEntry entry = plugin.getStorage().getPasswordEntry(player.getUniqueId());
    if (entry == null) {
      player.sendMessage(plugin.getConfiguration().getString("messages.have_to_register_first"));
      return;
    }

    BCrypt.Result result =
        BCrypt.verifyer().verify(password.toCharArray(), entry.getPassword().toCharArray());
    if (!result.verified) {
      player.sendMessage(plugin.getConfiguration().getString("messages.wrong_password"));
      return;
    }

    plugin.getSessionHandler().addLoggedIn(player.getUniqueId());
    player.sendMessage(plugin.getConfiguration().getString("messages.login_successful"));
  }

  public static void handleRegister(SimpleRegister plugin, Player player, String[] args) {
    if (plugin.getStorage().getPasswordEntry(player.getUniqueId()) != null) {
      player.sendMessage(plugin.getConfiguration().getString("messages.already_registered"));
      return;
    }
    String ip = player.getAddress().getAddress().getHostName();
    List<PasswordEntry> accounts = plugin.getStorage().getAltAccounts(ip);
    if (accounts.size() == plugin.getConfiguration().getInt("max_accounts_allowed")) {
      player.kickPlayer(plugin.getConfiguration().getString("messages.max_accounts_exceeded"));
      return;
    }

    if (args.length < 2) {
      player.sendMessage(plugin.getConfiguration().getString("messages.invalid_register_usage"));
      return;
    }

    String password = args[0];
    String confirmationPassword = args[1];
    if (!confirmationPassword.equalsIgnoreCase(password)) {
      player.sendMessage(plugin.getConfiguration().getString("messages.invalid_confirmation"));
      return;
    }

    String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
    PasswordEntry entry = new PasswordEntry(player.getUniqueId(), ip, hashedPassword);
    plugin.getStorage().addPassword(entry);

    plugin.getSessionHandler().addLoggedIn(player.getUniqueId());
    player.sendMessage(plugin.getConfiguration().getString("messages.register_successful"));
  }
}
