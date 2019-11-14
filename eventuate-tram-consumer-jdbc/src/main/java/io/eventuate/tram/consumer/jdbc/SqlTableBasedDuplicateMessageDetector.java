package io.eventuate.tram.consumer.jdbc;

import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.common.SubscriberIdAndMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

public class SqlTableBasedDuplicateMessageDetector implements DuplicateMessageDetector {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private EventuateSchema eventuateSchema;
  private String currentTimeInMillisecondsSql;
  private final TransactionTemplate transactionTemplate;


  public SqlTableBasedDuplicateMessageDetector(EventuateSchema eventuateSchema, String currentTimeInMillisecondsSql, TransactionTemplate transactionTemplate) {
    this.transactionTemplate = transactionTemplate;
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

  @Override
  public void doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Runnable callback) {
    transactionTemplate.execute(ts -> {
      try {
        if (!isDuplicate(subscriberIdAndMessage.getSubscriberId(), subscriberIdAndMessage.getMessage().getId()))
          callback.run();
        return null;
      } catch (Throwable e) {
        logger.trace("Got exception - marking for rollback only", e);
        ts.setRollbackOnly();
        throw e;
      }
    });
  }
}
