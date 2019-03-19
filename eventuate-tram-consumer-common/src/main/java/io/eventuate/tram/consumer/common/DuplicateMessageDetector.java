package io.eventuate.tram.consumer.common;

public interface DuplicateMessageDetector {
  boolean isDuplicate(String consumerId, String messageId);
  void doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Runnable callback);
}
