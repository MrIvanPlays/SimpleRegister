package com.mrivanplays.simpleregister.storage;

import java.util.UUID;

public final class PasswordEntry {

  private final int id;
  private final String name;
  private final UUID playerUUID;
  private final String playerIP;
  private final String password;

  public PasswordEntry(int id, String name, UUID playerUUID, String playerIP, String password) {
    this.id = id;
    this.name = name;
    this.playerUUID = playerUUID;
    this.playerIP = playerIP;
    this.password = password;
  }

  public int getId() {
    return id;
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
