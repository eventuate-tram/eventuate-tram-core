package io.eventuate.tram.messaging.producer.jdbc;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateCommonJdbcOperations;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;

public class MessageProducerJdbcImpl implements MessageProducerImplementation {

  private EventuateCommonJdbcOperations eventuateCommonJdbcOperations;
  private IdGenerator idGenerator;

  private EventuateSchema eventuateSchema;
  private String currentTimeInMillisecondsSql;


  public MessageProducerJdbcImpl(EventuateCommonJdbcOperations eventuateCommonJdbcOperations,
                                 IdGenerator idGenerator,
                                 EventuateSchema eventuateSchema,
                                 String currentTimeInMillisecondsSql) {

    this.eventuateCommonJdbcOperations = eventuateCommonJdbcOperations;
    this.idGenerator = idGenerator;
    this.eventuateSchema = eventuateSchema;
    this.currentTimeInMillisecondsSql = currentTimeInMillisecondsSql;
  }

  @Override
  public String generateMessageId() {
    return idGenerator.genId().asString();
  }


  @Override
  public void send(Message message) {
      eventuateCommonJdbcOperations.insertIntoMessageTable(message.getId(),
              message.getPayload(),
              message.getRequiredHeader(Message.DESTINATION),
              currentTimeInMillisecondsSql,
              message.getHeaders(),
              eventuateSchema);
  }
}
