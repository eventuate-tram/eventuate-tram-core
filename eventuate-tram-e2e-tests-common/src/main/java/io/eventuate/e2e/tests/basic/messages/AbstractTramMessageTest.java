package io.eventuate.e2e.tests.basic.messages;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractTramMessageTest {

  private long uniqueId = System.currentTimeMillis();

  private String subscriberId = "subscriberId" + uniqueId;
  private String destination = "destination" + uniqueId;
  private String payload = "Hello" + uniqueId;

  @Autowired
  private MessageProducer messageProducer;

  @Autowired
  private MessageConsumer messageConsumer;

  private BlockingQueue<Message> queue = new LinkedBlockingDeque<>();

  @Test
  public void shouldReceiveMessage() throws InterruptedException {
    messageConsumer.subscribe(subscriberId, Collections.singleton(destination), this::handleMessage);
    messageProducer.send(destination, MessageBuilder.withPayload(payload).build());

    Message m = queue.poll(60, TimeUnit.SECONDS);

    assertNotNull(m);
    assertEquals(payload, m.getPayload());
  }

  private void handleMessage(Message message) {
    queue.add(message);
  }
}
