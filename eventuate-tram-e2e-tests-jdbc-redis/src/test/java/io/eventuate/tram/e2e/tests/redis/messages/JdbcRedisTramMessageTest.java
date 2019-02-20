package io.eventuate.tram.e2e.tests.redis.messages;

import io.eventuate.e2e.tests.basic.messages.AbstractTramMessageTest;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = JdbcRedisTramMessageTestConfiguration.class)
public class JdbcRedisTramMessageTest extends AbstractTramMessageTest {
}
