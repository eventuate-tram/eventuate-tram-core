package io.eventuate.tram.consumer.jdbc.spring;

import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.common.SubscriberIdAndMessage;
import io.eventuate.tram.consumer.jdbc.AbstractSqlTableBasedDuplicateMessageDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

public class EventuateSpringSqlTableBasedDuplicateMessageDetector extends AbstractSqlTableBasedDuplicateMessageDetector {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private final TransactionTemplate transactionTemplate;

  public EventuateSpringSqlTableBasedDuplicateMessageDetector(EventuateSchema eventuateSchema,
                                                              String currentTimeInMillisecondsSql,
                                                              TransactionTemplate transactionTemplate,
                                                              EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor) {

    super(eventuateSchema, currentTimeInMillisecondsSql, eventuateJdbcStatementExecutor);
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public boolean isDuplicate(String consumerId, String messageId) {
    try {
      tryInsertMessage(consumerId, messageId);
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
