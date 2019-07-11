package io.eventuate.tram.consumer.jdbc.spring;

import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.common.SubscriberIdAndMessage;
import org.springframework.transaction.support.TransactionTemplate;

public class EventuateSpringTransactionalNoopDuplicateMessageDetector implements DuplicateMessageDetector {

  private TransactionTemplate transactionTemplate;

  public EventuateSpringTransactionalNoopDuplicateMessageDetector(TransactionTemplate transactionTemplate) {
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
