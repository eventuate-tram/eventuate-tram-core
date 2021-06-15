package io.eventuate.tram.consumer.common;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;

public interface DuplicateMessageDetector {
  boolean isDuplicate(String consumerId, String messageId);
  void doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Runnable callback);
}
