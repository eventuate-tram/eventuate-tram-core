package io.eventuate.tram.messaging.producer.jdbc;

import io.eventuate.common.id.generator.IdGenerator;
import io.eventuate.common.jdbc.EventuateCommonJdbcOperations;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import org.springframework.beans.factory.annotation.Autowired;

public class MessageProducerJdbcImpl implements MessageProducerImplementation {

  @Autowired
  private EventuateCommonJdbcOperations eventuateCommonJdbcOperations;

  @Autowired
  private IdGenerator idGenerator;

  private EventuateSchema eventuateSchema;
  private String currentTimeInMillisecondsSql;


  public MessageProducerJdbcImpl(EventuateSchema eventuateSchema, String currentTimeInMillisecondsSql) {
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
