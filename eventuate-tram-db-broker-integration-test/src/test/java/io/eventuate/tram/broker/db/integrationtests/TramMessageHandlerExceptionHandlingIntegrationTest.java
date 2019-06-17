package io.eventuate.tram.broker.db.integrationtests;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
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
import java.util.concurrent.TimeUnit;

import static io.eventuate.util.test.async.Eventually.eventually;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TramIntegrationTestConfiguration.class)
public class TramMessageHandlerExceptionHandlingIntegrationTest  {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  protected MessageProducer messageProducer;

  @Autowired
  protected MessageConsumer messageConsumer;

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

    MessageHandler handler = mock(MessageHandler.class);

    messageConsumer.subscribe(subscriberId, Collections.singleton(destination), handler);

    eventually(40, 1, TimeUnit.SECONDS, () -> verify(handler, times(2)).accept(any()));

  }

  private void sendMessageToFailingHandler() throws InterruptedException {

    MessageHandler handler = mock(MessageHandler.class);

    doThrow(new RuntimeException("x")).when(handler).accept(any());

    Message m1 = MessageBuilder.withPayload("Hello").withHeader(Message.PARTITION_ID, "1").build();
    Message m2 = MessageBuilder.withPayload("Hi").withHeader(Message.PARTITION_ID, "1").build();

    MessageSubscription subscription = messageConsumer.subscribe(subscriberId, Collections.singleton(destination), handler);

    messageProducer.send(destination, m1);
    messageProducer.send(destination, m2);

    logger.info("m1 = {}", m1.getId());
    logger.info("m2 = {}", m2.getId());


    eventually(40, 1, TimeUnit.SECONDS, () -> verify(handler).accept(any()));

    TimeUnit.SECONDS.sleep(5);

    verify(handler, times(1)).accept(any());

    subscription.unsubscribe();
  }

}
