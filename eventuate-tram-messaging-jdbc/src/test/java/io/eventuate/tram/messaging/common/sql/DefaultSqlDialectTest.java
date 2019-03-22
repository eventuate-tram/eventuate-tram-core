package io.eventuate.tram.messaging.common.sql;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SqlDialectConfiguration.class,
        properties= {"spring.datasource.driver-class-name=no.Matter",
                "eventuate.current.time.in.milliseconds.sql=some custom sql"})
public class DefaultSqlDialectTest extends AbstractDialectTest {
  public DefaultSqlDialectTest() {
    super(DefaultSqlDialect.class, "some custom sql");
  }
}
