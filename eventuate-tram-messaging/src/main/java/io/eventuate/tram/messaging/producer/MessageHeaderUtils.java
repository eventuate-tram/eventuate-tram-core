package io.eventuate.tram.messaging.producer;

import io.eventuate.tram.messaging.common.Message;

import java.util.UUID;

public class MessageHeaderUtils {

  public static void prepareMessageHeaders(String destination, Message message) {
    if (message.getHeaders().get(Message.ID) == null)
      message.getHeaders().put(Message.ID, UUID.randomUUID().toString());
    setDestinationDateAndPartitionIdHeaders(destination, message);
  }

  public static void setDestinationDateAndPartitionIdHeaders(String destination, Message message) {
    message.getHeaders().put(Message.DESTINATION, destination);
    message.getHeaders().put(Message.DATE, HttpDateHeaderFormatUtil.nowAsHttpDateString());
    if (message.getHeaders().get(Message.PARTITION_ID) == null)
      message.getHeaders().put(Message.PARTITION_ID, UUID.randomUUID().toString());
  }
}
