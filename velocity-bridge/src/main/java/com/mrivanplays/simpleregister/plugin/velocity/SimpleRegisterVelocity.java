package com.mrivanplays.simpleregister.plugin.velocity;

import com.google.inject.Inject;
import com.mrivanplays.simpleregister.plugin.velocity.listeners.CommandExecuteListener;
import com.mrivanplays.simpleregister.plugin.velocity.listeners.DisconnectListener;
import com.mrivanplays.simpleregister.plugin.velocity.listeners.PluginMessageListener;
import com.mrivanplays.simpleregister.plugin.velocity.listeners.ServerConnectedListener;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SimpleRegisterVelocity {

  public static final ChannelIdentifier SIMPLEREGISTER_PLUGIN_CHANNEL =
      MinecraftChannelIdentifier.create("simpleregister", "plugin");

  private final ProxyServer proxy;

  private Set<UUID> loggedIn;

  @Inject
  public SimpleRegisterVelocity(ProxyServer proxy) {
    this.proxy = proxy;
    this.loggedIn = new HashSet<>();
  }

  @Subscribe
  public void initialize(ProxyInitializeEvent event) {
    proxy.getChannelRegistrar().register(SIMPLEREGISTER_PLUGIN_CHANNEL);
    proxy
        .getEventManager()
        .register(this, PluginMessageEvent.class, new PluginMessageListener(this));
    proxy
        .getEventManager()
        .register(this, CommandExecuteEvent.class, new CommandExecuteListener(this));
    proxy
        .getEventManager()
        .register(this, ServerConnectedEvent.class, new ServerConnectedListener(this));
    proxy.getEventManager().register(this, DisconnectEvent.class, new DisconnectListener(this));
  }

  public ProxyServer getProxy() {
    return proxy;
  }

  public Set<UUID> getLoggedIn() {
    return loggedIn;
  }
}
