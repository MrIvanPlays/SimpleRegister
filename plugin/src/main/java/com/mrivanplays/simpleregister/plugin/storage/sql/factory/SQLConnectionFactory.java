package com.mrivanplays.simpleregister.plugin.storage.sql.factory;

import java.sql.Connection;
import java.sql.SQLException;

public interface SQLConnectionFactory {

  void connect() throws SQLException;

  Connection getConnection() throws SQLException;

  void close() throws SQLException;
}
