package io.eventuate.tram.messaging.common.sql;

import org.springframework.beans.factory.annotation.Value;

public class DefaultSqlDialect implements EventuateSqlDialect {
  @Value("${eventuate.current.time.in.milliseconds.sql:#{null}}")
  private String customSql;

  @Override
  public boolean supports(String driver) {
    return customSql != null;
  }

  @Override
  public String getCurrentTimeInMillisecondsExpression() {
    return customSql;
  }

  @Override
  public int getOrder() {
    return LOWEST_PRECEDENCE;
  }
}
