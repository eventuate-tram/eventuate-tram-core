package io.eventuate.tram.spring.flyway;

import java.sql.SQLException;

interface SqlExecutor {
  void execute(String ddl) throws SQLException;
}
