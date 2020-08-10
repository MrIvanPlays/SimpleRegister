package com.mrivanplays.simpleregister.plugin.velocity.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.mrivanplays.simpleregister.plugin.velocity.SimpleRegisterVelocity;
import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import java.util.UUID;

public class PluginMessageListener implements EventHandler<PluginMessageEvent> {

  private final SimpleRegisterVelocity plugin;

  public PluginMessageListener(SimpleRegisterVelocity plugin) {
    this.plugin = plugin;
  }

  @Override
  public void execute(PluginMessageEvent event) {
    if (!event
        .getIdentifier()
        .getId()
        .equalsIgnoreCase(SimpleRegisterVelocity.SIMPLEREGISTER_PLUGIN_CHANNEL.getId())) {
      return;
    }

    ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
    String subchannel = in.readUTF();
    if ("LoggedIn".equals(subchannel)) {
      UUID uuid = UUID.fromString(in.readUTF());
      plugin.getLoggedIn().add(uuid);
    }
  }
}
