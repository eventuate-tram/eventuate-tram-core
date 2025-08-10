package io.eventuate.tram.messaging.producer.common;

import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class MessageProducerImplTest {

  @Test
  public void shouldSendMessage() {
    ChannelMapping channelMapping = mock(ChannelMapping.class);

    MessageProducerImplementation implementation = mock(MessageProducerImplementation.class);

    MessageProducerImpl mp = new MessageProducerImpl(new MessageInterceptor[0], channelMapping, implementation);

    String transformedDestination = "TransformedDestination";
    String messageID = "1";

    doAnswer((Answer<Void>) invocation -> {
      ((Message)invocation.getArgument(0)).setHeader(Message.ID, messageID);
      return null;
    }).when(implementation).setMessageIdIfNecessary(any(Message.class));

    doAnswer((Answer<Void>) invocation -> {
      ((Runnable)invocation.getArgument(0)).run();
      return null;
    }).when(implementation).withContext(any(Runnable.class));

    when(channelMapping.transform("Destination")).thenReturn(transformedDestination);

    mp.send("Destination", MessageBuilder.withPayload("x").build());

    ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
    verify(implementation).send(messageArgumentCaptor.capture());
    Message sendMessage = messageArgumentCaptor.getValue();

    assertEquals(messageID, sendMessage.getRequiredHeader(Message.ID));
    assertEquals(transformedDestination, sendMessage.getRequiredHeader(Message.DESTINATION));
    assertNotNull(sendMessage.getRequiredHeader(Message.DATE));
  }
}