package com.mrivanplays.simpleregister;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerSessionHandler {

  private List<UUID> loggedIn;

  public PlayerSessionHandler() {
    loggedIn = new ArrayList<>();
  }

  public boolean hasLoggedIn(UUID uuid) {
    return loggedIn.contains(uuid);
  }

  public void addLoggedIn(UUID uuid) {
    loggedIn.add(uuid);
  }

  public void onQuit(UUID uuid) {
    loggedIn.remove(uuid);
  }
}
