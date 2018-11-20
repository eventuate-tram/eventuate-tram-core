package io.eventuate.tram.messaging.common.sql;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SqlDialectConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:default.dialect.properties")
public class DefaultSqlDialectTest extends AbstractDialectTest {
  public DefaultSqlDialectTest() {
    super(DefaultSqlDialect.class);
  }
}
