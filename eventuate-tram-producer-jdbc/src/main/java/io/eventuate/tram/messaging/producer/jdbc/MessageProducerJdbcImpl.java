package io.eventuate.tram.messaging.producer.jdbc;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.javaclient.spring.jdbc.IdGenerator;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class MessageProducerJdbcImpl implements MessageProducer {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private IdGenerator idGenerator;

  @Override
  public void send(String destination, Message message) {
    String id = idGenerator.genId().asString();
    message.getHeaders().put(Message.ID, id);
    jdbcTemplate.update("insert into message(id, destination, headers, payload) values(?, ?, ?, ?)",
            id,
            destination,
            JSonMapper.toJson(message.getHeaders()),
            message.getPayload());
  }

}
