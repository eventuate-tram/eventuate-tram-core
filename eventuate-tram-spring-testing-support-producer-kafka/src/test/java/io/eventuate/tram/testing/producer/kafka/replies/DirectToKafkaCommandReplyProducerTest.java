package io.eventuate.tram.testing.producer.kafka.replies;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaNativeCluster;
import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaNativeContainer;
import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder;
import io.eventuate.tram.commands.consumer.CommandReplyToken;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.spring.consumer.common.TramNoopDuplicateMessageDetectorConfiguration;
import io.eventuate.tram.spring.consumer.kafka.EventuateTramKafkaMessageConsumerConfiguration;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.testutil.TestMessageConsumer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = DirectToKafkaCommandReplyProducerTest.Config.class)
public class DirectToKafkaCommandReplyProducerTest {

  @Configuration
  @EnableAutoConfiguration
  @EnableDirectToKafkaCommandReplyProducer
  @Import({EventuateTramKafkaMessageConsumerConfiguration.class, TramNoopDuplicateMessageDetectorConfiguration.class})
  static class Config {
  }

  public static EventuateKafkaNativeCluster eventuateKafkaCluster =
      new EventuateKafkaNativeCluster("direct-kafka-command-reply-producer-test");

  public static EventuateKafkaNativeContainer kafka = eventuateKafkaCluster.kafka
      .withReuse(false);

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    Startables.deepStart(kafka).join();
    kafka.registerProperties(registry::add);
  }

  @Autowired
  private DirectToKafkaCommandReplyProducer commandReplyProducer;

  @Autowired
  private MessageConsumer messageConsumer;

  @Test
  public void shouldSendReplyToKafka() {
    String replyChannel = "TestReplyChannel-" + System.currentTimeMillis();
    CommandReplyToken replyToken = new CommandReplyToken(Collections.emptyMap(), replyChannel);
    Message reply = CommandHandlerReplyBuilder.withSuccess();

    TestMessageConsumer testConsumer = TestMessageConsumer.subscribeTo(messageConsumer, replyChannel);

    commandReplyProducer.sendReplies(replyToken, reply);

    testConsumer.assertHasMessages();
  }

  @Test
  public void shouldSetRequiredHeaders() {
    String replyChannel = "TestReplyChannel-" + System.currentTimeMillis();
    CommandReplyToken replyToken = new CommandReplyToken(Collections.emptyMap(), replyChannel);
    Message reply = CommandHandlerReplyBuilder.withSuccess();

    TestMessageConsumer testConsumer = TestMessageConsumer.subscribeTo(messageConsumer, replyChannel);

    commandReplyProducer.sendReplies(replyToken, reply);

    Message receivedMessage = testConsumer.assertHasMessage();

    assertEquals(replyChannel, receivedMessage.getRequiredHeader(Message.DESTINATION));
    assertNotNull(receivedMessage.getRequiredHeader(Message.DATE), "DATE header should be set");
    assertNotNull(receivedMessage.getRequiredHeader(Message.PARTITION_ID), "PARTITION_ID header should be set");
  }

  @Test
  public void shouldSendMultipleReplies() {
    String replyChannel = "TestReplyChannel-" + System.currentTimeMillis();
    CommandReplyToken replyToken = new CommandReplyToken(Collections.emptyMap(), replyChannel);
    Message reply1 = CommandHandlerReplyBuilder.withSuccess();
    Message reply2 = CommandHandlerReplyBuilder.withSuccess();

    TestMessageConsumer testConsumer = TestMessageConsumer.subscribeTo(messageConsumer, replyChannel);

    commandReplyProducer.sendReplies(replyToken, List.of(reply1, reply2));

    testConsumer.assertHasMessages();
  }

  @Test
  public void shouldIncludeCorrelationHeaders() {
    String replyChannel = "TestReplyChannel-" + System.currentTimeMillis();
    Map<String, String> correlationHeaders = Map.of("command_id", "test-command-123");
    CommandReplyToken replyToken = new CommandReplyToken(correlationHeaders, replyChannel);
    Message reply = CommandHandlerReplyBuilder.withSuccess();

    TestMessageConsumer testConsumer = TestMessageConsumer.subscribeTo(messageConsumer, replyChannel);

    commandReplyProducer.sendReplies(replyToken, reply);

    Message receivedMessage = testConsumer.assertHasMessage();

    assertEquals("test-command-123", receivedMessage.getRequiredHeader("command_id"));
  }

  public record TestCommand(String customerId) implements Command {
  }

  public record TestReply(String result) {
  }

  @Test
  public void shouldSendReplyFromCommandMessage() {
    String replyChannel = "TestReplyChannel-" + System.currentTimeMillis();

    Message commandMessage = MessageBuilder.withPayload(JSonMapper.toJson(new TestCommand("customer-123")))
        .withHeader(CommandMessageHeaders.COMMAND_TYPE, TestCommand.class.getName())
        .withHeader(CommandMessageHeaders.REPLY_TO, replyChannel)
        .withHeader(ReplyMessageHeaders.IN_REPLY_TO, "original-message-id")
        .build();

    TestMessageConsumer testConsumer = TestMessageConsumer.subscribeTo(messageConsumer, replyChannel);

    commandReplyProducer.sendReply(commandMessage, TestCommand.class, new TestReply("success"));

    Message receivedMessage = testConsumer.assertHasMessage();
    assertNotNull(receivedMessage);
    assertEquals(replyChannel, receivedMessage.getRequiredHeader(Message.DESTINATION));
  }
}
