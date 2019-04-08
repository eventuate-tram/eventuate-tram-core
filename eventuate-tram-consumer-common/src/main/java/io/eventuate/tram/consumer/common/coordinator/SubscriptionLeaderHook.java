package io.eventuate.tram.consumer.common.coordinator;

public interface SubscriptionLeaderHook {
  void leaderUpdated(Boolean leader, String subscriptionId);
}
