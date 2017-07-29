package io.eventuate.tram.consumer.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

public class DuplicateMessageDetector {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public boolean isDuplicate(String consumerId, String messageId) {
    try {
      jdbcTemplate.update("insert into received_messages(consumer_id, message_id) values(?, ?)",
              consumerId, messageId);
      return false;
    } catch (DuplicateKeyException e) {
      return true;
    }
  }
}
