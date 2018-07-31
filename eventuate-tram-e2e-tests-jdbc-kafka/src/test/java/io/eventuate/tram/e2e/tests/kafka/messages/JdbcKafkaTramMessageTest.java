package io.eventuate.tram.e2e.tests.kafka.messages;

import io.eventuate.e2e.tests.basic.messages.AbstractTramMessageTest;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = JdbcKafkaTramMessageTestConfiguration.class)
public class JdbcKafkaTramMessageTest extends AbstractTramMessageTest {
}
