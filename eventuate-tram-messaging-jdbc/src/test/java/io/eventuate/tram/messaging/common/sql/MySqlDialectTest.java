package io.eventuate.tram.messaging.common.sql;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SqlDialectConfiguration.class,
        properties= {"spring.datasource.driver-class-name=com.mysql.jdbc.Driver"})
public class MySqlDialectTest extends AbstractDialectTest {
  public MySqlDialectTest() {
    super(MySqlDialect.class, "ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000)");
  }
}
