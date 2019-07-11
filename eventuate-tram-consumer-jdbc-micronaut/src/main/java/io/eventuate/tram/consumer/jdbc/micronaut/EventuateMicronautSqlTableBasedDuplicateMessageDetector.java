package io.eventuate.tram.consumer.jdbc.micronaut;

import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.micronaut.EventuateMicronautSqlException;
import io.eventuate.common.jdbc.micronaut.EventuateMicronautTransactionManagement;
import io.eventuate.tram.consumer.common.SubscriberIdAndMessage;
import io.eventuate.tram.consumer.jdbc.AbstractSqlTableBasedDuplicateMessageDetector;

public class EventuateMicronautSqlTableBasedDuplicateMessageDetector extends AbstractSqlTableBasedDuplicateMessageDetector {
  private EventuateMicronautTransactionManagement eventuateMicronautTransactionManagement;


  public EventuateMicronautSqlTableBasedDuplicateMessageDetector(EventuateSchema eventuateSchema,
                                                                 String currentTimeInMillisecondsSql,
                                                                 EventuateMicronautTransactionManagement eventuateMicronautTransactionManagement,
                                                                 EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor) {
    super(eventuateSchema, currentTimeInMillisecondsSql, eventuateJdbcStatementExecutor);
    this.eventuateMicronautTransactionManagement = eventuateMicronautTransactionManagement;
  }

  @Override
  public boolean isDuplicate(String consumerId, String messageId) {
    try {
      tryInsertMessage(consumerId, messageId);
      return false;
    } catch (EventuateMicronautSqlException e) {
      return true;
    }
  }

  @Override
  public void doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Runnable callback) {
    eventuateMicronautTransactionManagement.doWithTransaction(connection -> {
        if (!isDuplicate(subscriberIdAndMessage.getSubscriberId(), subscriberIdAndMessage.getMessage().getId())) {
          callback.run();
        }
    });
  }
}
