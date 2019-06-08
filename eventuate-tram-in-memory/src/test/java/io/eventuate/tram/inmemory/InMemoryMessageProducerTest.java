package io.eventuate.tram.inmemory;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = InMemoryMessageProducerTest.InMemoryMessagingTestConfiguration.class)
public class InMemoryMessageProducerTest {

  private String subscriberId;
  private String destination;
  private String payload;
  private MyMessageHandler mh;

  @Configuration
  @EnableAutoConfiguration
  @Import({TramInMemoryConfiguration.class})
  public static class InMemoryMessagingTestConfiguration {

  }

  @Autowired
  private InMemoryMessageProducer inMemoryMessageProducer;

  @Autowired
  private InMemoryMessageConsumer inMemoryMessageConsumer;

  @Autowired
  private TransactionTemplate transactionTemplate;


  class MyMessageHandler implements MessageHandler {

    private BlockingQueue<String> queue = new LinkedBlockingDeque<>();

    @Override
    public void accept(Message message) {
      try {
        queue.put(message.getPayload());
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    void shouldReceiveMessage(String payload) {
      String m;
      try {
        while ((m = queue.poll(1, TimeUnit.SECONDS)) != null) {
          if (payload.equals(m))
            return;
        }
        fail("Didn't find message with payload: " + payload);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Before
  public void setUp() {
    subscriberId = "subscriberId-" + System.currentTimeMillis();
    destination = "destination-" + System.currentTimeMillis();
    payload = "payload-" + System.currentTimeMillis();
    mh = new MyMessageHandler();
  }

  @Test
  public void shouldDeliverToMatchingSubscribers() {

    inMemoryMessageConsumer.subscribe(subscriberId, Collections.singleton(destination), mh);

    Message m = makeMessage();
    inMemoryMessageProducer.send(m);
    assertNotNull(m.getId());
    mh.shouldReceiveMessage(payload);

  }

  @Test
  public void shouldSetIdWithinTransaction() {
    Message m = makeMessage();
    transactionTemplate.execute((TransactionCallback<Void>) status -> {
      inMemoryMessageProducer.send(m);
      assertNotNull(m.getId());
      return null;
    });
  }

  @Test
  public void shouldDeliverToWildcardSubscribers() {

    inMemoryMessageConsumer.subscribe(subscriberId, Collections.singleton("*"), mh);

    Message m = makeMessage();

    inMemoryMessageProducer.send(m);

    mh.shouldReceiveMessage(payload);

  }

  private Message makeMessage() {
    return MessageBuilder.withPayload(payload).withHeader(Message.DESTINATION, destination).withHeader(Message.ID, "message-id").build();
  }

}