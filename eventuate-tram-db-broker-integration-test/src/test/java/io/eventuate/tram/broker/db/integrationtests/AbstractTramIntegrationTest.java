package io.eventuate.tram.broker.db.integrationtests;

import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractTramIntegrationTest {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  protected MessageProducer messageProducer;

  @Autowired
  protected MessageConsumer messageConsumer;

  @Test
  public void shouldDoSomething() throws InterruptedException {
    String destination = "Destination-" + System.currentTimeMillis();
    String subscriberId = "SubscriberId-" + System.currentTimeMillis();

    CountDownLatch latch = new CountDownLatch(1);

    MessageHandler handler = message -> {
      logger.info("Got message=" + message);
      latch.countDown();
    };

    messageConsumer.subscribe(subscriberId, Collections.singleton(destination), handler);

    messageProducer.send(destination, MessageBuilder.withPayload("\"Hello\"").build());

    preAssertCheck();

    assertTrue(latch.await(60, TimeUnit.SECONDS), "Expected message. Subscriber %s for destination %s: ".formatted(subscriberId, destination));

  }

  protected void preAssertCheck() {

  }

}
