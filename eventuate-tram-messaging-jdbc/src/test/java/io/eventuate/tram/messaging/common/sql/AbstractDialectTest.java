package io.eventuate.tram.messaging.common.sql;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDialectTest {
  @Autowired
  private SqlDialectSelector sqlDialectSelector;

  private Class<? extends EventuateSqlDialect> expectedDialectClass;
  private String expectedCurrentTimeInMillisecondsExpression;


  public AbstractDialectTest(Class<? extends EventuateSqlDialect> expectedDialectClass,
                             String expectedCurrentTimeInMillisecondsExpression) {
    this.expectedDialectClass = expectedDialectClass;
    this.expectedCurrentTimeInMillisecondsExpression = expectedCurrentTimeInMillisecondsExpression;
  }

  @Test
  public void testDialect() {
    Assert.assertEquals(expectedDialectClass, sqlDialectSelector.getDialect().getClass());

    Assert.assertEquals(expectedCurrentTimeInMillisecondsExpression,
            sqlDialectSelector.getDialect().getCurrentTimeInMillisecondsExpression());
  }
}
