package io.eventuate.tram.inmemory.test;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;

import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractInMemoryMessageProducerTest {
  protected String subscriberId;
  protected String destination;
  protected String payload;
  protected MyMessageHandler mh;

  protected abstract MessageProducer getMessageProducer();
  protected abstract MessageConsumer getMessageConsumer();
  protected abstract void executeInTransaction(Consumer<Runnable> callbackWithRollback);

  public void setUp() {
    subscriberId = "subscriberId-" + System.currentTimeMillis();
    destination = "destination-" + System.currentTimeMillis();
    payload = "payload-" + System.currentTimeMillis();
    mh = new MyMessageHandler();
  }

  public void shouldDeliverToMatchingSubscribers() {
    subscribe();
    Message m = sendMessage();
    assertNotNull(m.getId());
    assertMessageReceived();
  }

  public void shouldSetIdWithinTransaction() {
    Message m = makeMessage();
    executeInTransaction(rollbackCallback -> {
      getMessageProducer().send(destination, m);
      assertNotNull(m.getId());
    });
  }

  public void shouldDeliverToWildcardSubscribers() {
    wildcardSubscribe();
    sendMessage();
    assertMessageReceived();
  }

  public void shouldReceiveMessageAfterTransaction() {
    subscribe();
    executeInTransaction(rollbackCallback -> sendMessage());
    assertMessageReceived();
  }

  public void shouldNotReceiveMessageBeforeTransaction() {
    subscribe();

    executeInTransaction(rollbackCallback -> {
      sendMessage();
      assertMessageNotReceived();
    });

    assertMessageReceived();
  }

  public void shouldNotReceiveMessageAfterTransactionRollback() {
    subscribe();

    executeInTransaction(rollbackCallback -> {
      sendMessage();
      rollbackCallback.run();
    });

    assertMessageNotReceived();
  }

  protected void assertMessageReceived() {
    mh.assertMessageReceived(payload);
  }

  protected void assertMessageNotReceived() {
    mh.assertMessageNotReceived(payload);
  }

  protected Message sendMessage() {
    Message m = makeMessage();
    getMessageProducer().send(destination, m);
    return m;
  }

  protected void wildcardSubscribe() {
    subscribe("*");
  }

  protected void subscribe() {
    subscribe(destination);
  }

  protected void subscribe(String destination) {
    getMessageConsumer().subscribe(subscriberId, Collections.singleton(destination), mh);
  }

  protected Message makeMessage() {
    return MessageBuilder.withPayload(payload).withHeader(Message.DESTINATION, destination).build();
  }

  protected static class MyMessageHandler implements MessageHandler {

    protected BlockingQueue<String> queue = new LinkedBlockingDeque<>();

    @Override
    public void accept(Message message) {
      try {
        queue.put(message.getPayload());
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    void assertMessageReceived(String payload) {
      assertTrue(searchMessage(payload));
    }

    void assertMessageNotReceived(String payload) {
      assertFalse(searchMessage(payload));
    }

    boolean searchMessage(String payload) {
      String m;

      try {
        while ((m = queue.poll(3, TimeUnit.SECONDS)) != null) {
          if (payload.equals(m)) {
            return true;
          }
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      return false;
    }
  }
}