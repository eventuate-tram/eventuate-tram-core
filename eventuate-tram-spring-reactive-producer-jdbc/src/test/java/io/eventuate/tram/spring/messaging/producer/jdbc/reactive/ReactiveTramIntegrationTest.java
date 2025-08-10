package io.eventuate.tram.spring.messaging.producer.jdbc.reactive;

import io.eventuate.common.spring.jdbc.reactive.EventuateCommonReactiveMysqlConfiguration;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import io.eventuate.tram.spring.consumer.common.TramNoopDuplicateMessageDetectorConfiguration;
import io.eventuate.tram.spring.consumer.kafka.EventuateTramKafkaMessageConsumerConfiguration;
import io.eventuate.tram.spring.messaging.common.TramMessagingCommonAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.reactive.TransactionalOperator;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(classes = ReactiveTramIntegrationTest.Config.class)
public class ReactiveTramIntegrationTest {

  @Import({ReactiveTramMessageProducerJdbcConfiguration.class,
          EventuateTramKafkaMessageConsumerConfiguration.class,
          TramNoopDuplicateMessageDetectorConfiguration.class,
          TramMessagingCommonAutoConfiguration.class,
          EventuateCommonReactiveMysqlConfiguration.class})
  @Configuration
  public static class Config {
  }

  @Autowired
  protected ReactiveMessageProducer messageProducer;

  @Autowired
  protected MessageConsumer messageConsumer;

  @Autowired
  protected TransactionalOperator transactionalOperator;

  @Test
  public void shouldReceiveMessage() throws InterruptedException {
    String payload = "\"%s\"".formatted(generateId());
    String destination = generateId();
    String subscriberId = generateId();

    BlockingQueue<String> messages = new LinkedBlockingDeque<>();

    MessageHandler handler = message -> messages.add(message.getPayload());

    messageConsumer.subscribe(subscriberId, Collections.singleton(destination), handler);

    messageProducer
            .send(destination, MessageBuilder.withPayload(payload).build())
            .as(transactionalOperator::transactional)
            .block(Duration.ofSeconds(30));

    assertEquals(payload, messages.poll(30, TimeUnit.SECONDS));
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
