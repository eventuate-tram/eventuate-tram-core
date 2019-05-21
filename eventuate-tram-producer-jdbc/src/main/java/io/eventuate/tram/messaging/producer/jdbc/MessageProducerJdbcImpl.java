package io.eventuate.tram.messaging.producer.jdbc;

import io.eventuate.common.jdbc.EventuateCommonJdbcOperations;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.javaclient.spring.jdbc.IdGenerator;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.producer.AbstractMessageProducer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;

public class MessageProducerJdbcImpl extends AbstractMessageProducer implements MessageProducer {

  @Autowired
  private EventuateCommonJdbcOperations eventuateCommonJdbcOperations;

  @Autowired
  private IdGenerator idGenerator;

  private EventuateSchema eventuateSchema;
  private String currentTimeInMillisecondsSql;


  public MessageProducerJdbcImpl(EventuateSchema eventuateSchema, String currentTimeInMillisecondsSql, MessageInterceptor[] messageInterceptors) {
    super(messageInterceptors);
    this.eventuateSchema = eventuateSchema;
    this.currentTimeInMillisecondsSql = currentTimeInMillisecondsSql;
  }

  @Override
  public void send(String destination, Message message) {
    String id = idGenerator.genId().asString();
    sendMessage(id, destination, message, this::send);
  }

  private void send(Message message) {
    eventuateCommonJdbcOperations.insertIntoMessageTable(message.getId(),
            message.getPayload(),
            message.getRequiredHeader(Message.DESTINATION),
            currentTimeInMillisecondsSql,
            message.getHeaders(),
            eventuateSchema);
  }
}
