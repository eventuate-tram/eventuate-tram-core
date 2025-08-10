package io.eventuate.tram.consumer.jdbc;

import io.eventuate.common.jdbc.EventuateDuplicateKeyException;
import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.EventuateTransactionTemplate;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlTableBasedDuplicateMessageDetector implements DuplicateMessageDetector {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventuateSchema eventuateSchema;
  private String currentTimeInMillisecondsSql;
  private EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor;
  private EventuateTransactionTemplate eventuateTransactionTemplate;

  public SqlTableBasedDuplicateMessageDetector(EventuateSchema eventuateSchema,
                                               String currentTimeInMillisecondsSql,
                                               EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                               EventuateTransactionTemplate eventuateTransactionTemplate) {
    this.eventuateSchema = eventuateSchema;
    this.currentTimeInMillisecondsSql = currentTimeInMillisecondsSql;
    this.eventuateJdbcStatementExecutor = eventuateJdbcStatementExecutor;
    this.eventuateTransactionTemplate = eventuateTransactionTemplate;
  }

  @Override
  public boolean isDuplicate(String consumerId, String messageId) {
    try {
      String table = eventuateSchema.qualifyTable("received_messages");

      eventuateJdbcStatementExecutor.update("insert into %s(consumer_id, message_id, creation_time) values(?, ?, %s)".formatted(
              table,
              currentTimeInMillisecondsSql),
              consumerId,
              messageId);

      return false;
    } catch (EventuateDuplicateKeyException e) {
      logger.info("Message duplicate: consumerId = {}, messageId = {}", consumerId, messageId);
      return true;
    }
  }

  @Override
  public void doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Runnable callback) {
    eventuateTransactionTemplate.executeInTransaction(() -> {
      if (!isDuplicate(subscriberIdAndMessage.getSubscriberId(), subscriberIdAndMessage.getMessage().getId()))
        callback.run();
      return null;
    });
  }
}
