package io.eventuate.tram.messaging.common.sql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.OrderComparator;

import javax.annotation.PostConstruct;
import java.util.Collection;

public class SqlDialectSelector {
  @Autowired
  private Collection<EventuateSqlDialect> sqlDialects;

  private String driver;
  private EventuateSqlDialect sqlDialect;

  public SqlDialectSelector(String driver) {
    this.driver = driver;
  }

  @PostConstruct
  private void init() {
    sqlDialect = sqlDialects
            .stream()
            .filter(dialect -> dialect.supports(driver))
            .min(OrderComparator.INSTANCE)
            .orElseThrow(() -> new IllegalStateException(String.format("Sql Dialect not found (%s)", driver)));
  }

  public EventuateSqlDialect getDialect() {
    return sqlDialect;
  }
}
