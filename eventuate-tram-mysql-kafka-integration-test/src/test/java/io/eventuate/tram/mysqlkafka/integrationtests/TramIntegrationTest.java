package io.eventuate.tram.mysqlkafka.integrationtests;

import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TramIntegrationTestConfiguration.class)
public class TramIntegrationTest {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private MessageProducer messageProducer;

  @Autowired
  private MessageConsumer messageConsumer;

  @Test
  public void shouldDoSomething() throws InterruptedException {
    String destination = "Destination-" + System.currentTimeMillis();
    String subscriberId = "SubscriberId-" + System.currentTimeMillis();

    CountDownLatch latch = new CountDownLatch(1);

    MessageHandler handler = message -> {
      System.out.println("Got message=" + message);
      latch.countDown();
    };

    messageConsumer.subscribe(subscriberId, Collections.singleton(destination), handler);

    messageProducer.send(destination, MessageBuilder.withPayload("Hello").build());

    assertTrue("Expected message", latch.await(10, TimeUnit.SECONDS));

  }
}
