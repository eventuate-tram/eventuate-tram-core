package io.eventuate.tram.spring.testing.consumer;

import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.spring.inmemory.EnableTramInMemory;
import io.eventuate.tram.testutil.TestMessageConsumer;
import io.eventuate.tram.testutil.TestMessageConsumerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = TestMessageConsumerFactoryTest.TestConfig.class)
public class TestMessageConsumerFactoryTest {

  @Configuration
  @EnableAutoConfiguration
  @EnableTestConsumer
  @EnableTramInMemory
  public static class TestConfig {
  }

  @Autowired
  private TestMessageConsumerFactory testMessageConsumerFactory;

  @Autowired
  private MessageProducer messageProducer;

  @Test
  public void shouldCreateTestMessageConsumer() {
    TestMessageConsumer consumer = testMessageConsumerFactory.make();
    assertNotNull(consumer);
    assertNotNull(consumer.getReplyChannel());
  }

  @Test
  public void shouldReceiveMessage() {
    TestMessageConsumer consumer = testMessageConsumerFactory.make();

    Message message = MessageBuilder.withPayload("{}")
            .withHeader(Message.DESTINATION, consumer.getReplyChannel())
            .build();
    messageProducer.send(consumer.getReplyChannel(), message);

    consumer.assertHasMessage();
  }

  @Test
  public void shouldDetectReplyTo() {
    TestMessageConsumer consumer = testMessageConsumerFactory.make();
    String commandId = "command-123";

    Message reply = MessageBuilder.withPayload("{}")
            .withHeader(Message.DESTINATION, consumer.getReplyChannel())
            .withHeader(ReplyMessageHeaders.IN_REPLY_TO, commandId)
            .build();
    messageProducer.send(consumer.getReplyChannel(), reply);

    consumer.assertHasReplyTo(commandId);
    assertTrue(consumer.containsReplyTo(commandId));
  }

  @Test
  public void shouldDetectReplyToWithType() {
    TestMessageConsumer consumer = testMessageConsumerFactory.make();
    String commandId = "command-456";

    Message reply = MessageBuilder.withPayload("{}")
            .withHeader(Message.DESTINATION, consumer.getReplyChannel())
            .withHeader(ReplyMessageHeaders.IN_REPLY_TO, commandId)
            .withHeader(ReplyMessageHeaders.REPLY_TYPE, String.class.getName())
            .build();
    messageProducer.send(consumer.getReplyChannel(), reply);

    consumer.assertHasReplyTo(commandId, String.class);
  }
}
