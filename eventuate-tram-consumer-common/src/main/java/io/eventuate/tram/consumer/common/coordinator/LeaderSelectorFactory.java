package io.eventuate.tram.consumer.common.coordinator;

public interface LeaderSelectorFactory {
  CommonLeaderSelector create(Runnable leaderSelectedCallback, Runnable leaderRemovedCallback);
}
