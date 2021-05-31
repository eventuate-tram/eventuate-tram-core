package io.eventuate.tram.spring.reactive.consumer.jdbc;

import io.eventuate.common.spring.jdbc.reactive.EventuateCommonReactiveDatabaseConfiguration;
import io.eventuate.tram.consumer.common.reactive.ReactiveDuplicateMessageDetector;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EventuateSpringReactiveSqlTableBasedDuplicateMessageDetectorTest.Config.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class EventuateSpringReactiveSqlTableBasedDuplicateMessageDetectorTest {

  @Autowired
  private ReactiveDuplicateMessageDetector duplicateMessageDetector;

  @Configuration
  @EnableAutoConfiguration
  @Import({EventuateCommonReactiveDatabaseConfiguration.class})
  public static class Config {
  }

  @Test
  public void shouldDetectDuplicate() {
    String consumerId = getClass().getName();
    String messageId = Long.toString(System.currentTimeMillis());

    Message message = new MessageImpl();
    message.setHeader(Message.ID, messageId);

    SubscriberIdAndMessage subscriberIdAndMessage = new SubscriberIdAndMessage(consumerId, message);

    assertFalse(duplicateMessageDetector.isDuplicate(subscriberIdAndMessage).block());
    assertTrue(duplicateMessageDetector.isDuplicate(subscriberIdAndMessage).block());
  }

}