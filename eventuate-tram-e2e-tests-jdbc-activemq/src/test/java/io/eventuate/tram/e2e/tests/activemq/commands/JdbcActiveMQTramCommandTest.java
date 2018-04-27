package io.eventuate.tram.e2e.tests.activemq.commands;

import io.eventuate.e2e.tests.basic.commands.AbstractTramCommandTest;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = JdbcActiveMQTramCommandTestConfiguration.class)
public class JdbcActiveMQTramCommandTest extends AbstractTramCommandTest {
}
