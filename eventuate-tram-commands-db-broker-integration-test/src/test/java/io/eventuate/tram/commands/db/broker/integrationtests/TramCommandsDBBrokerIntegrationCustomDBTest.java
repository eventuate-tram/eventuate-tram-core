package io.eventuate.tram.commands.db.broker.integrationtests;

import io.eventuate.local.testutil.CustomDBCreator;
import io.eventuate.local.testutil.CustomDBTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {TramCommandsDBBrokerIntegrationTestConfiguration.class, CustomDBTestConfiguration.class})
public class TramCommandsDBBrokerIntegrationCustomDBTest extends AbstractTramCommandsDBBrokerIntegrationTest {

  @Autowired
  private CustomDBCreator customDBCreator;

  @BeforeEach
  public void createCustomDB() {
    customDBCreator.create();
  }
}
