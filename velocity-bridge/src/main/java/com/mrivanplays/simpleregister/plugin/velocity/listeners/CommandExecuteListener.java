package com.mrivanplays.simpleregister.plugin.velocity.listeners;

import com.mrivanplays.simpleregister.plugin.velocity.SimpleRegisterVelocity;
import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.command.CommandExecuteEvent.CommandResult;
import com.velocitypowered.api.proxy.Player;

public class CommandExecuteListener implements EventHandler<CommandExecuteEvent> {

  private final SimpleRegisterVelocity plugin;

  public CommandExecuteListener(SimpleRegisterVelocity plugin) {
    this.plugin = plugin;
  }

  @Override
  public void execute(CommandExecuteEvent event) {
    if (event.getCommandSource() instanceof Player) {
      Player source = (Player) event.getCommandSource();
      if (!plugin.getLoggedIn().contains(source.getUniqueId())) {
        event.setResult(CommandResult.forwardToServer());
      }
    }
  }
}
