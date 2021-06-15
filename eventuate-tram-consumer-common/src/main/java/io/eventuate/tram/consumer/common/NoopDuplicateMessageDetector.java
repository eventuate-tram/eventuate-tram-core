package io.eventuate.tram.consumer.common;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;

public class  NoopDuplicateMessageDetector implements DuplicateMessageDetector {

  @Override
  public boolean isDuplicate(String consumerId, String messageId) {
    return false;
  }

  @Override
  public void doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Runnable callback) {
    callback.run();
  }
}
