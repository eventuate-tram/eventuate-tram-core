package io.eventuate.tram.mysqlkafka.integrationtests;

import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TramIntegrationTestConfiguration.class)
public class TramIntegrationTest extends AbstractTramIntegrationTest {

  private String destination;
  private String subscriberId;

  @Test
  public void shouldInvokeFailingHandler() throws InterruptedException {
    destination = "Destination-" + System.currentTimeMillis();
    subscriberId = "SubscriberId-" + System.currentTimeMillis();

    sendMessageToFailingHandler();

    assertMessageShouldBeReceivedByFixedHandler();

  }

  private void assertMessageShouldBeReceivedByFixedHandler() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);

    MessageHandler handler = message -> {
      logger.info("Got message=" + message);
      latch.countDown();
    };

    messageConsumer.subscribe(subscriberId, Collections.singleton(destination), handler);

    assertTrue(String.format("Expected message. Subscriber %s for destination %s: ", subscriberId, destination), latch.await(30, TimeUnit.SECONDS));
  }

  private void sendMessageToFailingHandler() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);

    MessageHandler handler = message -> {
      latch.countDown();
      throw new RuntimeException("x");
    };

    MessageSubscription subscription = messageConsumer.subscribe(subscriberId, Collections.singleton(destination), handler);

    messageProducer.send(destination, MessageBuilder.withPayload("Hello").build());

    assertTrue("Expected failing handler to be invoked", latch.await(30, TimeUnit.SECONDS));

    TimeUnit.SECONDS.sleep(1);

    subscription.unsubscribe();
  }

}
