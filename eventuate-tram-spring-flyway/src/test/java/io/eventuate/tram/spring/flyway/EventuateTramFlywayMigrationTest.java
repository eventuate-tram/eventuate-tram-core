package io.eventuate.tram.spring.flyway;

import io.eventuate.common.jdbc.OutboxPartitioningSpec;
import io.eventuate.common.testcontainers.DatabaseContainerFactory;
import io.eventuate.common.testcontainers.EventuateDatabaseContainer;
import io.eventuate.common.testcontainers.PropertyProvidingContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
public class EventuateTramFlywayMigrationTest {

  public static EventuateDatabaseContainer<?> database = DatabaseContainerFactory.makeVanillaDatabaseContainer();

  @DynamicPropertySource
  static void registerMySqlProperties(DynamicPropertyRegistry registry) {
    PropertyProvidingContainer.startAndProvideProperties(registry, database);
  }


  @Configuration
  @EnableAutoConfiguration
  @Import(EventuateTramFlywayMigrationConfiguration.class)
  public static class Config {

    @Bean
    OutboxPartitioningSpec outboxPartitioningSpec() {
      return OutboxPartitioningSpec.DEFAULT;
    }

  }

  @Autowired
  private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

  @Test
  public void shouldApplyMigration() {
    jdbcTemplate.execute("select * from message");

  }
}