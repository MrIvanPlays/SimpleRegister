package com.mrivanplays.simpleregister.plugin.velocity.listeners;

import com.mrivanplays.simpleregister.plugin.velocity.SimpleRegisterVelocity;
import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.connection.DisconnectEvent;

public class DisconnectListener implements EventHandler<DisconnectEvent> {

  private final SimpleRegisterVelocity plugin;

  public DisconnectListener(SimpleRegisterVelocity plugin) {
    this.plugin = plugin;
  }

  @Override
  public void execute(DisconnectEvent event) {
    plugin.getLoggedIn().remove(event.getPlayer().getUniqueId());
  }
}
