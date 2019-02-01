package io.eventuate.tram.messaging.common.sql;

public class PostgreSqlDialect implements EventuateSqlDialect {
  @Override
  public boolean supports(String driver) {
    return "org.postgresql.Driver".equals(driver);
  }

  @Override
  public String getCurrentTimeInMillisecondsExpression() {
    return "(ROUND(EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000))";
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE;
  }
}
