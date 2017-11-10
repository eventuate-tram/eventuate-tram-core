package io.eventuate.tram.consumer.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

public class SqlTableBasedDuplicateMessageDetector implements DuplicateMessageDetector {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private String database;

  public SqlTableBasedDuplicateMessageDetector(String database) {
    this.database = database;
  }

  @Override
  public boolean isDuplicate(String consumerId, String messageId) {
    try {
      jdbcTemplate.update(String.format("insert into %s.received_messages(consumer_id, message_id) values(?, ?)", database),
              consumerId, messageId);
      return false;
    } catch (DuplicateKeyException e) {
      return true;
    }
  }
}
