package io.eventuate.tram.messaging.producer.jdbc;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateCommonJdbcOperations;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;

public class MessageProducerJdbcImpl implements MessageProducerImplementation {

  private final EventuateCommonJdbcOperations eventuateCommonJdbcOperations;
  private final IdGenerator idGenerator;

  private final EventuateSchema eventuateSchema;


  public MessageProducerJdbcImpl(EventuateCommonJdbcOperations eventuateCommonJdbcOperations,
                                 IdGenerator idGenerator,
                                 EventuateSchema eventuateSchema) {

    this.eventuateCommonJdbcOperations = eventuateCommonJdbcOperations;
    this.idGenerator = idGenerator;
    this.eventuateSchema = eventuateSchema;
  }

  @Override
  public void send(Message message) {
    String id =  eventuateCommonJdbcOperations.insertIntoMessageTable(idGenerator,
              message.getPayload(),
              message.getRequiredHeader(Message.DESTINATION),
              message.getHeaders(),
              eventuateSchema);

    message.setHeader(Message.ID, id);
  }
}
