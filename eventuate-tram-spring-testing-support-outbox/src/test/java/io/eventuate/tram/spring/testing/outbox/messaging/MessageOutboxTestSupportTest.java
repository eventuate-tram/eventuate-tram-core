package io.eventuate.tram.spring.testing.outbox.messaging;

import io.eventuate.common.testcontainers.DatabaseContainerFactory;
import io.eventuate.common.testcontainers.EventuateDatabaseContainer;
import io.eventuate.common.testcontainers.PropertyProvidingContainer;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;
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
public class MessageOutboxTestSupportTest {

  public static EventuateDatabaseContainer<?> database = DatabaseContainerFactory.makeVanillaDatabaseContainer();

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    PropertyProvidingContainer.startAndProvideProperties(registry, database);
  }

  @Configuration
  @EnableAutoConfiguration
  @EnableMessageOutboxTestSupport
  @Import({EventuateTramFlywayMigrationConfiguration.class,
      TramMessageProducerJdbcConfiguration.class})
  public static class Config {
  }

  @Autowired
  private MessageProducer messageProducer;

  @Autowired
  private MessageOutboxTestSupport messageOutboxTestSupport;

  private String testChannel;

  @BeforeEach
  void setUp() {
    testChannel = "test-channel-" + System.currentTimeMillis();
  }

  @Test
  void shouldFindMessagesSentToChannel() {
    Message message = MessageBuilder.withPayload("{\"data\":\"test\"}").build();
    messageProducer.send(testChannel, message);

    List<Message> messages = messageOutboxTestSupport.findMessagesSentToChannel(testChannel);

    assertThat(messages).hasSize(1);
    assertThat(messages.get(0).getPayload()).isEqualTo("{\"data\":\"test\"}");
  }

  @Test
  void shouldReturnEmptyListForEmptyChannel() {
    List<Message> messages = messageOutboxTestSupport.findMessagesSentToChannel("empty-channel-" + System.currentTimeMillis());

    assertThat(messages).isEmpty();
  }
}
