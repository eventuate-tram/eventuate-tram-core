package io.eventuate.tram.commandsandevents.integrationtests;

import io.eventuate.local.testutil.CustomDBCreator;
import io.eventuate.local.testutil.CustomDBTestConfiguration;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TramCommandsAndEventsIntegrationTestConfiguration.class, CustomDBTestConfiguration.class})
public class TramCommandsAndEventsIntegrationCustomDBTest extends AbstractTramCommandsAndEventsIntegrationTest {

  @Autowired
  private CustomDBCreator customDBCreator;

  @Before
  public void createCustomDB() {
    customDBCreator.create();
  }
}
