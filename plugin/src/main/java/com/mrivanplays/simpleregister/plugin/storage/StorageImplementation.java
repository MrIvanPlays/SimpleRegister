package com.mrivanplays.simpleregister.plugin.storage;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface StorageImplementation {

  void connect();

  List<PasswordEntry> getPasswords();

  void addPassword(PasswordEntry entry);

  void modifyPassword(UUID owner, PasswordEntry entry);

  void removeEntry(UUID owner);

  void close();
}
