package io.eventuate.e2e.tests.basic.commands;

import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractTramCommandTest {

  @Autowired
  private CommandProducer commandProducer;

  @Autowired
  private  AbstractTramCommandTestConfig config;

  @Autowired
  private MessageConsumer messageConsumer;

  private BlockingQueue<Message> queue = new LinkedBlockingDeque<>();

  @Test
  public void shouldInvokeCommand() throws InterruptedException {
    String subscriberId = "subscriberId" + config.getUniqueId();
    messageConsumer.subscribe(subscriberId, Collections.singleton(config.getCustomerChannel()), this::handleMessage);
    String commandId = commandProducer.send(config.getCommandChannel(),
            new DoSomethingCommand(),
            config.getCustomerChannel(),
            Collections.emptyMap());

    Message m = queue.poll(10, TimeUnit.SECONDS);

    System.out.println("Got message = " + m);

    assertNotNull(m);
    assertEquals(commandId, m.getRequiredHeader(ReplyMessageHeaders.IN_REPLY_TO));

  }

  private void handleMessage(Message message) {
    queue.add(message);
  }
}
