package io.eventuate.tram.consumer.common.coordinator;

public interface LeaderSelectorFactory {
  CommonLeaderSelector create(String groupId, Runnable leaderSelectedCallback, Runnable leaderRemovedCallback);
}
