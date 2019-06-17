package io.eventuate.tram.commands.db.borker.integrationtests;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TramCommandsDBBrokerIntegrationTestConfiguration.class)
public class TramCommandsDBBrokerIntegrationTest extends AbstractTramCommandsDBBrokerIntegrationTest {
}
