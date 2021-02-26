package io.eventuate.tram.consumer.jdbc;

import io.eventuate.common.jdbc.EventuateTransactionTemplate;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.common.SubscriberIdAndMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionalNoopDuplicateMessageDetector implements DuplicateMessageDetector {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventuateTransactionTemplate transactionTemplate;

  public TransactionalNoopDuplicateMessageDetector(EventuateTransactionTemplate transactionTemplate) {
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public boolean isDuplicate(String consumerId, String messageId) {
    return false;
  }

  @Override
  public void doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Runnable callback) {
    transactionTemplate.executeInTransaction(() -> {
      try {
        callback.run();
        return null;
      } catch (Throwable e) {
        logger.error("Got exception - marking for rollback only", e);
        throw e;
      }
    });
  }
}
