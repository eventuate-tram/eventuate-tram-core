package io.eventuate.tram.commandsandevents.integrationtests;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TramCommandsAndEventsIntegrationTestConfiguration.class, TramCommandsAndEventsIntegrationCustomDBTest.Configuration.class})
public class TramCommandsAndEventsIntegrationCustomDBTest extends AbstractTramCommandsAndEventsIntegrationTest {

  @org.springframework.context.annotation.Configuration
  @EnableAutoConfiguration
  @PropertySource({"/customdb.properties"})
  public static class Configuration {
  }

  @Autowired
  private DataSource dataSource;

  @Before
  public void createDefaultDB() {
    Resource resource = new ClassPathResource("custom-db-mysql-schema.sql");
    ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
    databasePopulator.execute(dataSource);
  }
}
