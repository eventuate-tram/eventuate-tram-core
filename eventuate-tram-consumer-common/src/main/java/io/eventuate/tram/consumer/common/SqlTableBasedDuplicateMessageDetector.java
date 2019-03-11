package io.eventuate.tram.consumer.common;

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
  private String currentTimeInMillisecondsSql;

  public SqlTableBasedDuplicateMessageDetector(EventuateSchema eventuateSchema, String currentTimeInMillisecondsSql) {
    this.eventuateSchema = eventuateSchema;
    this.currentTimeInMillisecondsSql = currentTimeInMillisecondsSql;
  }

  @Override
  public boolean isDuplicate(String consumerId, String messageId) {
    try {

      String table = eventuateSchema.qualifyTable("received_messages");

      jdbcTemplate.update(String.format("insert into %s(consumer_id, message_id, creation_time) values(?, ?, %s)",
              table,
              currentTimeInMillisecondsSql),
              consumerId, messageId);

      return false;
    } catch (DuplicateKeyException e) {
      return true;
    }
  }
}
