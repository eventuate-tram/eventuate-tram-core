package io.eventuate.tram.messaging.common.sql;

import org.springframework.core.Ordered;

public interface EventuateSqlDialect extends Ordered {
  boolean supports(String driver);
  String getCurrentTimeInMillisecondsExpression();
}
