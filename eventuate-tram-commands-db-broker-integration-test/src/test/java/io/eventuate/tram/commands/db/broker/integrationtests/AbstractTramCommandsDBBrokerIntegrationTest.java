package io.eventuate.tram.commands.db.broker.integrationtests;

import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.PathVariables;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.messaging.common.Message;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

public abstract class AbstractTramCommandsDBBrokerIntegrationTest {

  @Autowired
  private CommandProducer commandProducer;

  @Autowired
  private MyReplyConsumer myReplyConsumer;

  @Autowired
  private MyTestCommandHandler myTestCommandHandler;

  @Test
  public void shouldDoSomething() throws InterruptedException {
    String messageId = commandProducer.send("customerService", "/customers/10",
            new MyTestCommand(), myReplyConsumer.getReplyChannel(),
            Collections.emptyMap());

    Message m = myReplyConsumer.messages.poll(60, TimeUnit.SECONDS);

    assertNotNull(m);

    assertEquals(messageId, m.getRequiredHeader(ReplyMessageHeaders.IN_REPLY_TO));

    System.out.println("Received m=" + m);

    verify(myTestCommandHandler).myHandlerMethod(any(CommandMessage.class), any(PathVariables.class));
  }
}
