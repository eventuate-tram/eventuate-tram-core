package io.eventuate.tram.commandsandevents.integrationtests;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TramCommandsAndEventsIntegrationTestConfiguration.class)
public class TramCommandsAndEventsIntegrationTest extends AbstractTramCommandsAndEventsIntegrationTest{
}
