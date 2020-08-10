package com.mrivanplays.simpleregister.plugin.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.mrivanplays.simpleregister.plugin.SimpleRegisterPlugin;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class PluginMessageReceiver implements PluginMessageListener {

  private final SimpleRegisterPlugin plugin;

  public PluginMessageReceiver(SimpleRegisterPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void onPluginMessageReceived(String channel, Player player, byte[] data) {
    if (!channel.equalsIgnoreCase("simpleregister:plugin")) {
      return;
    }
    ByteArrayDataInput in = ByteStreams.newDataInput(data);
    String subchannel = in.readUTF();
    if (subchannel.equalsIgnoreCase("AlreadyLoggedIn")) {
      plugin.getSessionHandler().addLoggedIn(UUID.fromString(in.readUTF()));
    }
  }
}
