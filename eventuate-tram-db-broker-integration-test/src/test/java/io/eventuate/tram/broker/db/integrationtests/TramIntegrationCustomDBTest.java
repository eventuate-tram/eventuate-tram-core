package io.eventuate.tram.broker.db.integrationtests;

import io.eventuate.local.testutil.CustomDBCreator;
import io.eventuate.local.testutil.CustomDBTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {CustomDBTestConfiguration.class, TramIntegrationTestConfiguration.class})
public class TramIntegrationCustomDBTest extends AbstractTramIntegrationTest {

  @Autowired
  private CustomDBCreator customDBCreator;

  @BeforeEach
  public void createCustomDB() {
    customDBCreator.create();
  }
}
