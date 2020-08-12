package com.mrivanplays.simpleregister.plugin.storage;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface StorageImplementation {

  void connect();

  List<PasswordEntry> getPasswords();

  void addPassword(PasswordEntry entry);

  default PasswordEntry getPasswordEntry(UUID uuid) {
    return getPasswords().stream()
        .filter(entry -> entry.getPlayerUUID().equals(uuid))
        .findFirst()
        .orElse(null);
  }

  void modifyPassword(UUID owner, PasswordEntry entry);

  void removeEntry(UUID owner);

  void close();
}
