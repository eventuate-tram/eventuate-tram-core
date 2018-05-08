package io.eventuate.tram.e2e.tests.rabbitmq.events;

import io.eventuate.e2e.tests.basic.events.AbstractTramEventTest;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = JdbcRabbitMQTramEventTestConfiguration.class)
public class JdbcRabbitMQTramEventTest extends AbstractTramEventTest {
}
