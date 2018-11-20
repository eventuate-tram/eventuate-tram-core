package io.eventuate.tram.messaging.common.sql;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDialectTest {

  private Class<? extends EventuateSqlDialect> expectedDialectClass;

  @Autowired
  private SqlDialectSelector sqlDialectSelector;

  public AbstractDialectTest(Class<? extends EventuateSqlDialect> expectedDialectClass) {
    this.expectedDialectClass = expectedDialectClass;
  }

  @Test
  public void testDialect() {
    Assert.assertEquals(expectedDialectClass, sqlDialectSelector.getDialect().getClass());
  }
}
