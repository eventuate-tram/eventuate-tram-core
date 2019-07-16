package io.eventuate.messaging.kafka.basic.consumer;

import org.apache.kafka.common.TopicPartition;

import java.util.HashSet;
import java.util.Set;

public class BackPressureManagerPausedState implements BackPressureManagerState {

  private Set<TopicPartition> suspendedPartitions;

  public BackPressureManagerPausedState(Set<TopicPartition> pausedTopic) {
    this.suspendedPartitions = new HashSet<>(pausedTopic);
  }

  public static BackPressureManagerStateAndActions transitionTo(Set<TopicPartition> allTopicPartitions) {
    return new BackPressureManagerStateAndActions(BackPressureActions.pause(allTopicPartitions), new BackPressureManagerPausedState(allTopicPartitions));
  }


  @Override
  public BackPressureManagerStateAndActions update(Set<TopicPartition> allTopicPartitions, int backlog, BackPressureConfig backPressureConfig) {
    if (backlog <= backPressureConfig.getLow()) {
      return BackPressureManagerNormalState.transitionTo(suspendedPartitions);
    } else {
      Set<TopicPartition> toSuspend = new HashSet<>(allTopicPartitions);
      toSuspend.removeAll(suspendedPartitions);
      suspendedPartitions.addAll(toSuspend);
      return new BackPressureManagerStateAndActions(BackPressureActions.pause(toSuspend), this);
    }
  }
}
