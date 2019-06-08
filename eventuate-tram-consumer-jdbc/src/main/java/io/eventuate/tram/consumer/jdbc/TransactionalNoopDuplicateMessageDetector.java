package io.eventuate.tram.consumer.jdbc;

import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.common.SubscriberIdAndMessage;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalNoopDuplicateMessageDetector implements DuplicateMessageDetector {

  private TransactionTemplate transactionTemplate;

  public TransactionalNoopDuplicateMessageDetector(TransactionTemplate transactionTemplate) {
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public boolean isDuplicate(String consumerId, String messageId) {
    return false;
  }

  @Override
  public void doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Runnable callback) {
    transactionTemplate.execute(ts -> {
      try {
        callback.run();
        return null;
      } catch (Throwable e) {
        ts.setRollbackOnly();
        throw e;
      }
    });

  }
}
