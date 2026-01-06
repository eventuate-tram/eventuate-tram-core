package io.eventuate.tram.testing.producer.kafka.commands;

import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaNativeCluster;
import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaNativeContainer;
import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.spring.consumer.common.TramNoopDuplicateMessageDetectorConfiguration;
import io.eventuate.tram.spring.consumer.kafka.EventuateTramKafkaMessageConsumerConfiguration;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.testutil.TestMessageConsumer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.lifecycle.Startables;

import java.util.Collections;

@SpringBootTest(classes = DirectToKafkaCommandProducerTest.Config.class)
public class DirectToKafkaCommandProducerTest {

  @Configuration
  @EnableAutoConfiguration
  @EnableDirectToKafkaCommandProducer
  @Import({EventuateTramKafkaMessageConsumerConfiguration.class, TramNoopDuplicateMessageDetectorConfiguration.class})
  static class Config {
  }

  public static EventuateKafkaNativeCluster eventuateKafkaCluster =
      new EventuateKafkaNativeCluster("direct-kafka-command-producer-test");

  public static EventuateKafkaNativeContainer kafka = eventuateKafkaCluster.kafka
      .withReuse(false);

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    Startables.deepStart(kafka).join();
    kafka.registerProperties(registry::add);
  }

  @Autowired
  private DirectToKafkaCommandProducer commandProducer;

  @Autowired
  private MessageConsumer messageConsumer;

  public record TestCommand(String data) implements Command {
  }

  @Test
  public void shouldSendCommandToKafka() {
    String channel = "TestCommandChannel-" + System.currentTimeMillis();
    String replyTo = "TestReplyChannel-" + System.currentTimeMillis();
    TestCommand command = new TestCommand("test-data");

    TestMessageConsumer testConsumer = TestMessageConsumer.subscribeTo(messageConsumer, channel);

    commandProducer.send(channel, command, replyTo, Collections.emptyMap());

    testConsumer.assertHasMessages();
  }

  @Test
  public void shouldSetRequiredHeaders() {
    String channel = "TestCommandChannel-" + System.currentTimeMillis();
    String replyTo = "TestReplyChannel-" + System.currentTimeMillis();
    TestCommand command = new TestCommand("test-data");

    TestMessageConsumer testConsumer = TestMessageConsumer.subscribeTo(messageConsumer, channel);

    commandProducer.send(channel, command, replyTo, Collections.emptyMap());

    Message receivedMessage = testConsumer.assertHasMessage();

    assertNotNull(receivedMessage.getHeader(Message.DESTINATION).orElse(null), "DESTINATION header should be set");
    assertNotNull(receivedMessage.getHeader(Message.DATE).orElse(null), "DATE header should be set");
    assertNotNull(receivedMessage.getHeader(Message.PARTITION_ID).orElse(null), "PARTITION_ID header should be set");
  }
}
