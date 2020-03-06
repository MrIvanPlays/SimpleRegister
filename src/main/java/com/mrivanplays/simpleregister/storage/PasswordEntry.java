package com.mrivanplays.simpleregister.storage;

import java.util.UUID;

public final class PasswordEntry {

  private final String name;
  private final UUID playerUUID;
  private final String playerIP;
  private final String password;

  public PasswordEntry(String name, UUID playerUUID, String playerIP, String password) {
    this.name = name;
    this.playerUUID = playerUUID;
    this.playerIP = playerIP;
    this.password = password;
  }

  public String getName() {
    return name;
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
