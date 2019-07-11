package io.eventuate.tram.consumer.jdbc.micronaut;

import io.eventuate.common.jdbc.micronaut.EventuateMicronautTransactionManagement;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.common.SubscriberIdAndMessage;

public class EventuateMicronautTransactionalNoopDuplicateMessageDetector implements DuplicateMessageDetector {

  private EventuateMicronautTransactionManagement eventuateMicronautTransactionManagement;

  public EventuateMicronautTransactionalNoopDuplicateMessageDetector(EventuateMicronautTransactionManagement eventuateMicronautTransactionManagement) {
    this.eventuateMicronautTransactionManagement = eventuateMicronautTransactionManagement;
  }

  @Override
  public boolean isDuplicate(String consumerId, String messageId) {
    return false;
  }

  @Override
  public void doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Runnable callback) {
    eventuateMicronautTransactionManagement.doWithTransaction(connection -> callback.run());
  }
}
