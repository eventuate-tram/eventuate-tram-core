package io.eventuate.tram.spring.flyway;

import io.eventuate.common.flyway.TemplatedMessageTableCreator;
import io.eventuate.common.jdbc.OutboxPartitioningSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventuateTramFlywayMigrationConfiguration {

  @Bean
  public V1005__MyMigration v1005__myMigration(TemplatedMessageTableCreator templatedMessageTableCreator, OutboxPartitioningSpec outboxPartitioningSpec) {
    return new V1005__MyMigration(templatedMessageTableCreator, outboxPartitioningSpec);
  }

  @Bean
  public TemplatedMessageTableCreator templatedMessageTableCreator() {
    return new TemplatedMessageTableCreator();
  }
}
