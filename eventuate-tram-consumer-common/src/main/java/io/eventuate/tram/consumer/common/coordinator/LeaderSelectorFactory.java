package io.eventuate.tram.consumer.common.coordinator;

public interface LeaderSelectorFactory {
  CommonLeaderSelector create(String groupId, String subscriptionId, Runnable leaderSelectedCallback, Runnable leaderRemovedCallback);
}
