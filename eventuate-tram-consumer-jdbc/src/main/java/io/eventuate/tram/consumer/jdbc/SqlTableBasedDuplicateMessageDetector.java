package io.eventuate.tram.consumer.jdbc;

import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.common.SubscriberIdAndMessage;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

public class SqlTableBasedDuplicateMessageDetector implements DuplicateMessageDetector {
  private EventuateSchema eventuateSchema;
  private String currentTimeInMillisecondsSql;
  private JdbcTemplate jdbcTemplate;
  private TransactionTemplate transactionTemplate;

  public SqlTableBasedDuplicateMessageDetector(EventuateSchema eventuateSchema,
                                               String currentTimeInMillisecondsSql,
                                               JdbcTemplate jdbcTemplate,
                                               TransactionTemplate transactionTemplate) {
    this.eventuateSchema = eventuateSchema;
    this.currentTimeInMillisecondsSql = currentTimeInMillisecondsSql;
    this.jdbcTemplate = jdbcTemplate;
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public boolean isDuplicate(String consumerId, String messageId) {
    try {
      String table = eventuateSchema.qualifyTable("received_messages");

      jdbcTemplate.update(String.format("insert into %s(consumer_id, message_id, creation_time) values(?, ?, %s)",
              table,
              currentTimeInMillisecondsSql),
              consumerId,
              messageId);

      return false;
    } catch (DuplicateKeyException e) {
      return true;
    }
  }

  @Override
  public void doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Runnable callback) {
    transactionTemplate.execute(ts -> {
      try {
        if (!isDuplicate(subscriberIdAndMessage.getSubscriberId(), subscriberIdAndMessage.getMessage().getId()))
          callback.run();
        return null;
      } catch (Throwable e) {
        ts.setRollbackOnly();
        throw e;
      }
    });
  }
}
