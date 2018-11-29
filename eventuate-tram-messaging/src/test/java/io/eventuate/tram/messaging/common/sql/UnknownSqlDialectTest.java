package io.eventuate.tram.messaging.common.sql;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

@RunWith(SpringRunner.class)
public class UnknownSqlDialectTest {

  @Test
  public void testDialect() {
    IllegalStateException exception = null;

    try {
      new SqlDialectSelector("unknown.UnknownDriver").selectDialect(Collections.emptySet());
    } catch (IllegalStateException e) {
      exception = e;
    }

    Assert.assertNotNull(exception);

    String expectedMessage = "Sql Dialect not found (unknown.UnknownDriver), " +
            "you can specify environment variable 'EVENTUATE_CURRENT_TIME_IN_MILLISECONDS_SQL' to solve the issue";

    Assert.assertEquals(expectedMessage, exception.getMessage());
  }
}
