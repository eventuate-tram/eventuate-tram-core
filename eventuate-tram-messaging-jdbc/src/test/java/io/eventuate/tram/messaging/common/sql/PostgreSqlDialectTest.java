package io.eventuate.tram.messaging.common.sql;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SqlDialectConfiguration.class,
        properties= {"spring.datasource.driver-class-name=org.postgresql.Driver"})
public class PostgreSqlDialectTest extends AbstractDialectTest {
  public PostgreSqlDialectTest() {
    super(PostgreSqlDialect.class, "(ROUND(EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000))");
  }
}
