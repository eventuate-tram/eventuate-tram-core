package io.eventuate.tram.spring.reactive.consumer.jdbc;

import io.eventuate.common.spring.jdbc.reactive.EventuateCommonReactiveDatabaseConfiguration;
import io.eventuate.tram.consumer.common.reactive.ReactiveDuplicateMessageDetector;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class EventuateSpringReactiveSqlTableBasedDuplicateMessageDetectorTest {

  @Autowired
  private ReactiveDuplicateMessageDetector duplicateMessageDetector;
  private SubscriberIdAndMessage subscriberIdAndMessage;

  @Configuration
  @EnableAutoConfiguration
  @Import({EventuateCommonReactiveDatabaseConfiguration.class})
  public static class Config {
  }

  @BeforeEach
  public void setUp() {
    String consumerId = getClass().getName();
    String messageId = Long.toString(System.currentTimeMillis());

    Message message = new MessageImpl();
    message.setHeader(Message.ID, messageId);

    subscriberIdAndMessage = new SubscriberIdAndMessage(consumerId, message);
  }

  @Test
  public void shouldDetectDuplicate() {
    assertFalse(duplicateMessageDetector.isDuplicate(subscriberIdAndMessage).block(Duration.ofSeconds(30)));
    assertTrue(duplicateMessageDetector.isDuplicate(subscriberIdAndMessage).block(Duration.ofSeconds(30)));
  }
  @Test

  public void shouldInvokeHandlerOrNot() {
    var result = Mono.from(duplicateMessageDetector.doWithMessage(subscriberIdAndMessage, Mono.just("processed")))
        .block(Duration.ofSeconds(30));

    assertEquals("processed", result);

    var result2 = Mono.from(duplicateMessageDetector.doWithMessage(subscriberIdAndMessage, Mono.just("processed")))
        .block(Duration.ofSeconds(30));

    assertNull(result2, "Expected no result for duplicate message processing");
  }

}