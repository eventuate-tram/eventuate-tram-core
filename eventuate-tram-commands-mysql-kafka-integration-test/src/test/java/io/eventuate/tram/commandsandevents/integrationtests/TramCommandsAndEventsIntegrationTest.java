package io.eventuate.tram.commandsandevents.integrationtests;

import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.messaging.common.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TramCommandsAndEventsIntegrationTestConfiguration.class)
public class TramCommandsAndEventsIntegrationTest {

  @Autowired
  private CommandProducer commandProducer;

  @Autowired
  private MyReplyConsumer myReplyConsumer;

  @Autowired
  private MyTestCommandHandler myTestCommandHandler;

  @Test
  public void shouldDoSomething() throws InterruptedException {
    String messageId = commandProducer.send("customerService", "/customers/10",
            new MyTestCommand(),
            Collections.emptyMap());

    Message m = myReplyConsumer.messages.poll(5, TimeUnit.SECONDS);

    assertNotNull(m);

    assertEquals(messageId, m.getRequiredHeader(ReplyMessageHeaders.IN_REPLY_TO));

    System.out.println("Received m=" + m);

    verify(myTestCommandHandler).myHandlerMethod(any(String.class), any(CommandMessage.class));
  }
}
