package io.eventuate.tram.consumer.kafka;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.util.test.async.Eventually;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class SwimlaneDispatcherTest {

  private SwimlaneDispatcher swimlaneDispatcher;
  private AtomicInteger numberOfMessagesReceived;
  private Consumer<Message> handler;

  @Before
  public void init() {
    swimlaneDispatcher = new SwimlaneDispatcher("1", 1, Executors.newCachedThreadPool());
    numberOfMessagesReceived = new AtomicInteger(0);
  }

  @Test
  public void shouldDispatchManyMessages() {
    int numberOfMessagesToSend = 5;

    createHandler();

    sendMessages(numberOfMessagesToSend);
    assertMessageReceived(numberOfMessagesToSend);
  }

  @Test
  public void testShouldRestart() {
    int numberOfMessagesToSend = 5;

    createHandler();

    sendMessages(numberOfMessagesToSend);
    assertDispatcherStopped();
    sendMessages(numberOfMessagesToSend);
    assertMessageReceived(numberOfMessagesToSend * 2);
  }

  private void createHandler() {
    handler = msg -> {
      numberOfMessagesReceived.incrementAndGet();
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    };
  }

  private void sendMessages(int numberOfMessagesToSend) {
    for (int i = 0; i < numberOfMessagesToSend; i++) {
      if (i > 0) {
        Assert.assertTrue(swimlaneDispatcher.getRunning());
      }
      swimlaneDispatcher.dispatch(MessageBuilder.withPayload("").build(), handler);
    }
  }

  private void assertMessageReceived(int numberOfMessagesToSend) {
    Eventually.eventually(() -> Assert.assertEquals(numberOfMessagesToSend, numberOfMessagesReceived.get()));
  }

  private void assertDispatcherStopped() {
    Eventually.eventually(() -> Assert.assertFalse(swimlaneDispatcher.getRunning()));
  }
}
