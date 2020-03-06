package com.mrivanplays.simpleregister.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogFilterHelper {

  static final List<String> COMMANDS_TO_SKIP =
      withAndWithoutPrefix(
          "/login ",
          "/l",
          "/register ",
          "/unregister ",
          "/changepassword ",
          "/simpleregister forceregister",
          "/simpleregister changepassword");

  private static final String ISSUED_COMMAND_TEXT = "issued server command:";

  private LogFilterHelper() {
    // Util class
  }

  static boolean isSensitiveCommand(String message) {
    if (message == null) {
      return false;
    }
    String lowerMessage = message.toLowerCase();
    return lowerMessage.contains(ISSUED_COMMAND_TEXT)
        && containsAny(lowerMessage, COMMANDS_TO_SKIP);
  }

  private static List<String> withAndWithoutPrefix(String... commands) {
    List<String> commandList = new ArrayList<>(commands.length * 2);
    for (String command : commands) {
      commandList.add(command);
      commandList.add(command.substring(0, 1) + "simpleregister:" + command.substring(1));
    }
    return Collections.unmodifiableList(commandList);
  }

  public static boolean containsAny(String str, Iterable<String> pieces) {
    if (str == null) {
      return false;
    }
    for (String piece : pieces) {
      if (piece != null && str.contains(piece)) {
        return true;
      }
    }
    return false;
  }
}
