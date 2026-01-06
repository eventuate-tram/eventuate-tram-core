package io.eventuate.tram.testing.producer.kafka.events;

import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaNativeCluster;
import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaNativeContainer;
import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import io.eventuate.tram.spring.consumer.common.TramNoopDuplicateMessageDetectorConfiguration;
import io.eventuate.tram.spring.consumer.kafka.EventuateTramKafkaMessageConsumerConfiguration;
import io.eventuate.tram.testutil.TestMessageConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.lifecycle.Startables;

@SpringBootTest(classes = DirectToKafkaDomainEventPublisherTest.Config.class)
public class DirectToKafkaDomainEventPublisherTest {

  @Configuration
  @EnableAutoConfiguration
  @EnableDirectToKafkaDomainEventPublisher
  @Import({EventuateTramKafkaMessageConsumerConfiguration.class, TramNoopDuplicateMessageDetectorConfiguration.class})
  static class Config {
  }

  public static EventuateKafkaNativeCluster eventuateKafkaCluster =
      new EventuateKafkaNativeCluster("direct-kafka-event-publisher-test");

  public static EventuateKafkaNativeContainer kafka = eventuateKafkaCluster.kafka
      .withReuse(false);

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    Startables.deepStart(kafka).join();
    kafka.registerProperties(registry::add);
  }

  @Autowired
  private DirectToKafkaDomainEventPublisher eventPublisher;

  @Autowired
  private MessageConsumer messageConsumer;

  public record TestEvent(String data) implements DomainEvent {
  }

  @Test
  public void shouldPublishDomainEventToKafka() {
    String aggregateType = "TestAggregate-" + System.currentTimeMillis();
    String aggregateId = "123";
    TestEvent event = new TestEvent("test-data");

    TestMessageConsumer testConsumer = TestMessageConsumer.subscribeTo(messageConsumer, aggregateType);

    eventPublisher.publish(aggregateType, aggregateId, event);

    testConsumer.assertHasMessages();
  }

  @Test
  public void shouldSetRequiredHeaders() {
    String aggregateType = "TestAggregate-" + System.currentTimeMillis();
    String aggregateId = "123";
    TestEvent event = new TestEvent("test-data");

    TestMessageConsumer testConsumer = TestMessageConsumer.subscribeTo(messageConsumer, aggregateType);

    eventPublisher.publish(aggregateType, aggregateId, event);

    Message receivedMessage = testConsumer.assertHasMessage();

    assertNotNull(receivedMessage.getHeader(Message.DESTINATION).orElse(null), "DESTINATION header should be set");
    assertNotNull(receivedMessage.getHeader(Message.DATE).orElse(null), "DATE header should be set");
    assertNotNull(receivedMessage.getHeader(Message.PARTITION_ID).orElse(null), "PARTITION_ID header should be set");
  }
}
