package io.eventuate.tram.consumer.kafka;

import io.eventuate.local.common.EventuateConstants;
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

      String table = EventuateConstants.EMPTY_DATABASE_SCHEMA.equals(database) ? "received_messages" : database + ".received_messages";


      jdbcTemplate.update(String.format("insert into %s(consumer_id, message_id) values(?, ?)", table),
              consumerId, messageId);
      return false;
    } catch (DuplicateKeyException e) {
      return true;
    }
  }
}
