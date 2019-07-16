package io.eventuate.messaging.kafka.basic.consumer;

import org.apache.kafka.common.TopicPartition;

import java.util.Set;

public class BackPressureManagerNormalState implements BackPressureManagerState {

  public static BackPressureManagerStateAndActions transitionTo(Set<TopicPartition> suspendedPartitions) {
    return new BackPressureManagerStateAndActions(BackPressureActions.resume(suspendedPartitions), new BackPressureManagerNormalState());
  }

  @Override
  public BackPressureManagerStateAndActions update(Set<TopicPartition> allTopicPartitions, int backlog, BackPressureConfig backPressureConfig) {
      if (backlog > backPressureConfig.getHigh()) {
        return BackPressureManagerPausedState.transitionTo(allTopicPartitions);
      } else {
        return new BackPressureManagerStateAndActions(this);
      }
  }

}
