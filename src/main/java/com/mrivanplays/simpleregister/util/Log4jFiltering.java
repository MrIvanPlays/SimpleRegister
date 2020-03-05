package com.mrivanplays.simpleregister.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

public class Log4jFiltering {

  public static void setup() {
    Logger logger = (Logger) LogManager.getRootLogger();
    logger.addFilter(new SensitiveCommandFilter());
  }

  private static final class SensitiveCommandFilter extends AbstractFilter {

    private static final long serialVersionUID = -5594073755007974254L;

    private static Result validateMessage(Message message) {
      if (message == null) {
        return Result.NEUTRAL;
      }
      return validateMessage(message.getFormattedMessage());
    }

    private static Result validateMessage(String message) {
      return LogFilterHelper.isSensitiveAuthMeCommand(message)
          ? Result.DENY
          : Result.NEUTRAL;
    }

    @Override
    public Result filter(LogEvent event) {
      Message candidate = null;
      if (event != null) {
        candidate = event.getMessage();
      }
      return validateMessage(candidate);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
      return validateMessage(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
      return validateMessage(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
      String candidate = null;
      if (msg != null) {
        candidate = msg.toString();
      }
      return validateMessage(candidate);
    }
  }
}
