package io.eventuate.tram.consumer.kafka;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.util.test.async.Eventually;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class SwimlaneDispatcherTest {

  @Test
  public void shouldDispatchManyMessages() {
    int numberOfMessagesToSend = 5;

    SwimlaneDispatcher swimlaneDispatcher = new SwimlaneDispatcher("1", 1, Executors.newCachedThreadPool());

    AtomicInteger numberOfMessagesReceived = new AtomicInteger(0);

    Consumer<Message> handler = createHandler(numberOfMessagesReceived);

    sendMessages(swimlaneDispatcher, handler, numberOfMessagesToSend);
    assertMessageReceived(numberOfMessagesReceived, numberOfMessagesToSend);
  }

  @Test
  public void testShouldRestart() {
    int numberOfMessagesToSend = 5;

    SwimlaneDispatcher swimlaneDispatcher = new SwimlaneDispatcher("1", 1, Executors.newCachedThreadPool());

    AtomicInteger numberOfMessagesReceived = new AtomicInteger(0);

    Consumer<Message> handler = createHandler(numberOfMessagesReceived);

    sendMessages(swimlaneDispatcher, handler, numberOfMessagesToSend);
    assertDispatcherStopped(swimlaneDispatcher);
    sendMessages(swimlaneDispatcher, handler, numberOfMessagesToSend);
    assertMessageReceived(numberOfMessagesReceived, numberOfMessagesToSend * 2);
  }

  private Consumer<Message> createHandler(AtomicInteger numberOfMessagesReceived) {
    return msg -> {
      numberOfMessagesReceived.incrementAndGet();
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    };
  }

  private void sendMessages(SwimlaneDispatcher swimlaneDispatcher, Consumer<Message> handler, int numberOfMessagesToSend) {
    for (int i = 0; i < numberOfMessagesToSend; i++) {
      if (i > 0) {
        Assert.assertTrue(swimlaneDispatcher.getRunning());
      }
      swimlaneDispatcher.dispatch(MessageBuilder.withPayload("").build(), handler);
    }
  }

  private void assertMessageReceived(AtomicInteger numberOfMessagesReceived, int numberOfMessagesToSend) {
    Eventually.eventually(() -> Assert.assertEquals(numberOfMessagesReceived.get(), numberOfMessagesToSend));
  }

  private void assertDispatcherStopped(SwimlaneDispatcher swimlaneDispatcher) {
    Eventually.eventually(() -> Assert.assertFalse(swimlaneDispatcher.getRunning()));
  }
}
