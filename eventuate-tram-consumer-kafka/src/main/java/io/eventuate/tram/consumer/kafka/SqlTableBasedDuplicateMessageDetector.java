package io.eventuate.tram.consumer.kafka;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

public class SqlTableBasedDuplicateMessageDetector implements DuplicateMessageDetector {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private EventuateSchema eventuateSchema;

  public SqlTableBasedDuplicateMessageDetector(EventuateSchema eventuateSchema) {
    this.eventuateSchema = eventuateSchema;
  }

  @Override
  public boolean isDuplicate(String consumerId, String messageId) {
    try {

      String table = eventuateSchema.qualifyTable("received_messages");

      jdbcTemplate.update(String.format("insert into %s(consumer_id, message_id) values(?, ?)", table),
              consumerId, messageId);
      return false;
    } catch (DuplicateKeyException e) {
      return true;
    }
  }
}
