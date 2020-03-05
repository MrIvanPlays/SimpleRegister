package com.mrivanplays.simpleregister.storage;

import java.util.UUID;

public final class PasswordEntry {

  private final UUID playerUUID;
  private final String playerIP;
  private final String password;

  public PasswordEntry(UUID playerUUID, String playerIP, String password) {
    this.playerUUID = playerUUID;
    this.playerIP = playerIP;
    this.password = password;
  }

  public UUID getPlayerUUID() {
    return playerUUID;
  }

  public String getPlayerIP() {
    return playerIP;
  }

  public String getPassword() {
    return password;
  }
}
