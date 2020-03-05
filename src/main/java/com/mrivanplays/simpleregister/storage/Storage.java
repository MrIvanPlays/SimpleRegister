package com.mrivanplays.simpleregister.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Storage {

  private File file;
  private Gson gson;
  private Type listType;

  public Storage(File dataFolder) {
    this.listType = new TypeToken<List<PasswordEntry>>() {}.getType();
    this.file = new File(dataFolder, "passwords.json");
    createFileIfNotExists();
    this.gson = new GsonBuilder().serializeNulls().create();
  }

  public List<PasswordEntry> getPasswords() {
    List<PasswordEntry> entries = new ArrayList<>();
    try (Reader reader = new FileReader(file)) {
      List<PasswordEntry> list = gson.fromJson(reader, listType);
      if (list == null) {
        return entries;
      }
      entries = list;
    } catch (IOException ignored) {
    }
    return entries;
  }

  public void addPassword(PasswordEntry entry) {
    List<PasswordEntry> passwords = getPasswords();
    passwords.add(entry);
    write(passwords);
  }

  public PasswordEntry getPasswordEntry(UUID uuid) {
    return getPasswords().stream()
        .filter(entry -> entry.getPlayerUUID().equals(uuid))
        .findFirst()
        .orElse(null);
  }

  public List<PasswordEntry> getAltAccounts(String ip) {
    return getPasswords().stream()
        .filter(entry -> entry.getPlayerIP().equalsIgnoreCase(ip))
        .collect(Collectors.toList());
  }

  public void modifyPassword(UUID owner, PasswordEntry passwordEntry) {
    List<PasswordEntry> passwords = getPasswords();
    passwords.replaceAll(
        password -> {
          if (password.getPlayerUUID().equals(owner)) {
            return passwordEntry;
          }
          return password;
        });
    write(passwords);
  }

  public void removeEntry(UUID uuid) {
    List<PasswordEntry> passwords = getPasswords();
    passwords.removeIf(entry -> entry.getPlayerUUID().equals(uuid));
    write(passwords);
  }

  private void write(List<PasswordEntry> entries) {
    file.delete();
    createFileIfNotExists();
    try (Writer writer = new FileWriter(file)) {
      gson.toJson(entries, listType, writer);
    } catch (IOException ignored) {
    }
  }

  private void createFileIfNotExists() {
    if (!file.exists()) {
      if (!file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
