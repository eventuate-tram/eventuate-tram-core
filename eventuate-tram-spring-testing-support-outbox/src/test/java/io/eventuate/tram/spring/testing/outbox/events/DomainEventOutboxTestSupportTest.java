package io.eventuate.tram.spring.testing.outbox.events;

import io.eventuate.common.testcontainers.DatabaseContainerFactory;
import io.eventuate.common.testcontainers.EventuateDatabaseContainer;
import io.eventuate.common.testcontainers.PropertyProvidingContainer;
import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.spring.events.publisher.TramEventsPublisherConfiguration;
import io.eventuate.tram.spring.flyway.EventuateTramFlywayMigrationConfiguration;
import io.eventuate.tram.spring.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DomainEventOutboxTestSupportTest {

  public static EventuateDatabaseContainer<?> database = DatabaseContainerFactory.makeVanillaDatabaseContainer();

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    PropertyProvidingContainer.startAndProvideProperties(registry, database);
  }

  @Configuration
  @EnableAutoConfiguration
  @EnableDomainEventOutboxTestSupport
  @Import({EventuateTramFlywayMigrationConfiguration.class,
      TramMessageProducerJdbcConfiguration.class,
      TramEventsPublisherConfiguration.class})
  public static class Config {
  }

  public record TestEvent(String data) implements DomainEvent {
  }

  @Autowired
  private DomainEventPublisher domainEventPublisher;

  @Autowired
  private DomainEventOutboxTestSupport domainEventOutboxTestSupport;

  private String aggregateType;
  private String aggregateId;

  @BeforeEach
  void setUp() {
    aggregateType = "TestAggregate-" + System.currentTimeMillis();
    aggregateId = "agg-" + System.currentTimeMillis();
  }

  @Test
  void shouldAssertDomainEventInOutbox() {
    domainEventPublisher.publish(aggregateType, aggregateId, List.of(new TestEvent("test-data")));

    domainEventOutboxTestSupport.assertDomainEventInOutbox(aggregateType, aggregateId, TestEvent.class.getName());
  }

  @Test
  void shouldFindEventsOfType() {
    domainEventPublisher.publish(aggregateType, aggregateId, List.of(new TestEvent("data1"), new TestEvent("data2")));

    List<TestEvent> events = domainEventOutboxTestSupport.findEventsOfType(aggregateType, TestEvent.class);

    assertThat(events).hasSize(2);
    assertThat(events).extracting(TestEvent::data).containsExactlyInAnyOrder("data1", "data2");
  }

  @Test
  void shouldFindEventMessagesOfType() {
    domainEventPublisher.publish(aggregateType, aggregateId, List.of(new TestEvent("test-data")));

    List<Message> messages = domainEventOutboxTestSupport.findEventMessagesOfType(aggregateType, TestEvent.class);

    assertThat(messages).hasSize(1);
  }
}
