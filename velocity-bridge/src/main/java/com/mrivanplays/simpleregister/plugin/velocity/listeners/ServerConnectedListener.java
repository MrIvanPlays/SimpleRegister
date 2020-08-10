package com.mrivanplays.simpleregister.plugin.velocity.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mrivanplays.simpleregister.plugin.velocity.SimpleRegisterVelocity;
import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.util.List;

public class ServerConnectedListener implements EventHandler<ServerConnectedEvent> {

  private final SimpleRegisterVelocity plugin;

  public ServerConnectedListener(SimpleRegisterVelocity plugin) {
    this.plugin = plugin;
  }

  @Override
  public void execute(ServerConnectedEvent event) {
    Player player = event.getPlayer();
    List<String> order = plugin.getProxy().getConfiguration().getAttemptConnectionOrder();
    if (event.getPreviousServer().isPresent()) {
      RegisteredServer current = event.getServer();
      if (order.contains(current.getServerInfo().getName())
          && plugin.getLoggedIn().contains(player.getUniqueId())) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("AlreadyLoggedIn");
        out.writeUTF(player.getUniqueId().toString());
        current.sendPluginMessage(
            SimpleRegisterVelocity.SIMPLEREGISTER_PLUGIN_CHANNEL, out.toByteArray());
      }
    }
  }
}
