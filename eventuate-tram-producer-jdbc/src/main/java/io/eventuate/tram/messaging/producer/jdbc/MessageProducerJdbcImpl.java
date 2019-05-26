package io.eventuate.tram.messaging.producer.jdbc;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.javaclient.spring.jdbc.IdGenerator;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class MessageProducerJdbcImpl implements MessageProducerImplementation {

  @Autowired
  private JdbcTemplate jdbcTemplate;

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
    String table = eventuateSchema.qualifyTable("message");
    jdbcTemplate.update(String.format("insert into %s(id, destination, headers, payload, creation_time) values(?, ?, ?, ?, %s)",
            table,
            currentTimeInMillisecondsSql),
            message.getId(),
            message.getRequiredHeader(Message.DESTINATION),
            JSonMapper.toJson(message.getHeaders()),
            message.getPayload());
  }
}
