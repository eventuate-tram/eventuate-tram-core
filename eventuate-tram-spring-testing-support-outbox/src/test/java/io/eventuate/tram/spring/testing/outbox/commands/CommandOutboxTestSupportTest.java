package io.eventuate.tram.spring.testing.outbox.commands;

import io.eventuate.common.testcontainers.DatabaseContainerFactory;
import io.eventuate.common.testcontainers.EventuateDatabaseContainer;
import io.eventuate.common.testcontainers.PropertyProvidingContainer;
import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandReplyProducer;
import io.eventuate.tram.commands.consumer.CommandReplyToken;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.spring.commands.producer.TramCommandProducerConfiguration;
import io.eventuate.tram.spring.commands.consumer.TramCommandReplyProducerConfiguration;
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

import java.util.Collections;
import java.util.Map;

import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withSuccess;

@SpringBootTest
public class CommandOutboxTestSupportTest {

  public static EventuateDatabaseContainer<?> database = DatabaseContainerFactory.makeVanillaDatabaseContainer();

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    PropertyProvidingContainer.startAndProvideProperties(registry, database);
  }

  @Configuration
  @EnableAutoConfiguration
  @Import({EventuateTramFlywayMigrationConfiguration.class,
      TramMessageProducerJdbcConfiguration.class,
      TramCommandProducerConfiguration.class,
      TramCommandReplyProducerConfiguration.class,
      CommandOutboxTestSupportConfiguration.class})
  public static class Config {
  }

  public record TestCommand(String customerId, String data) implements Command {
  }

  public record TestReply(String result) {
  }

  @Autowired
  private CommandProducer commandProducer;

  @Autowired
  private CommandReplyProducer commandReplyProducer;

  @Autowired
  private CommandOutboxTestSupport commandOutboxTestSupport;

  private String testChannel;
  private String replyChannel;

  @BeforeEach
  void setUp() {
    testChannel = "test-channel-" + System.currentTimeMillis();
    replyChannel = "reply-channel-" + System.currentTimeMillis();
  }

  @Test
  void shouldAssertCommandMessageSent() {
    TestCommand command = new TestCommand("customer-123", "test-data");
    commandProducer.send(testChannel, command, replyChannel, Collections.emptyMap());

    commandOutboxTestSupport.assertCommandMessageSent(testChannel, TestCommand.class);
  }

  @Test
  void shouldAssertCommandReplyMessageSent() {
    Map<String, String> replyHeaders = Map.of("reply_to_message_id", "msg-123");
    CommandReplyToken replyToken = new CommandReplyToken(replyHeaders, replyChannel);
    commandReplyProducer.sendReplies(replyToken, withSuccess(new TestReply("success")));

    commandOutboxTestSupport.assertCommandReplyMessageSent(replyChannel);
  }
}
