package io.eventuate.tram.messaging.producer;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AbstractMessageProducerTest {

  @Test
  public void shouldSendMessage() {
    MessageSender ms = mock(MessageSender.class, Mockito.CALLS_REAL_METHODS);
    AbstractMessageProducer mp = new AbstractMessageProducer(new MessageInterceptor[0]) {
    };
    mp.sendMessage("id", "Destination", MessageBuilder.withPayload("x").build(), ms);

    ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
    verify(ms).send(messageArgumentCaptor.capture());
    Message sendMessage = messageArgumentCaptor.getValue();

    assertNotNull(sendMessage.getRequiredHeader(Message.ID));
    assertNotNull(sendMessage.getRequiredHeader(Message.DESTINATION));
    assertNotNull(sendMessage.getRequiredHeader(Message.DATE));
  }
}