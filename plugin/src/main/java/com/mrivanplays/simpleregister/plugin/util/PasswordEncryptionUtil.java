package com.mrivanplays.simpleregister.plugin.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordEncryptionUtil {

  public static boolean verifyPasswords(String actual, String expected) {
    return BCrypt.verifyer().verify(actual.toCharArray(), expected.toCharArray()).verified;
  }

  public static String hash(String password) {
    return BCrypt.withDefaults().hashToString(12, password.toCharArray());
  }
}
