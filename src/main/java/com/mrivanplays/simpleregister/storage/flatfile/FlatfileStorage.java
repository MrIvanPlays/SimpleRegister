package com.mrivanplays.simpleregister.storage.flatfile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mrivanplays.simpleregister.storage.PasswordEntry;
import com.mrivanplays.simpleregister.storage.StorageImplementation;
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

public class FlatfileStorage implements StorageImplementation {

  private File file;
  private Gson gson;
  private Type listType;

  public FlatfileStorage(File dataFolder) {
    this.listType = new TypeToken<List<PasswordEntry>>() {}.getType();
    this.file = new File(dataFolder, "passwords.json");
    createFileIfNotExists();
    this.gson = new GsonBuilder().serializeNulls().create();
  }

  @Override
  public void connect() {}

  @Override
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

  @Override
  public void addPassword(PasswordEntry entry) {
    List<PasswordEntry> passwords = getPasswords();
    passwords.add(entry);
    write(passwords);
  }

  @Override
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

  @Override
  public void removeEntry(UUID uuid) {
    List<PasswordEntry> passwords = getPasswords();
    passwords.removeIf(entry -> entry.getPlayerUUID().equals(uuid));
    write(passwords);
  }

  @Override
  public void close() {}

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
