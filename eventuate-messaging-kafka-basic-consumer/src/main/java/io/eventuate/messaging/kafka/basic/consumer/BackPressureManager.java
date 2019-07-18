package io.eventuate.messaging.kafka.basic.consumer;

import org.apache.kafka.common.TopicPartition;

import java.util.HashSet;
import java.util.Set;

public class BackPressureManager {

  private final BackPressureConfig backPressureConfig;
  private final Set<TopicPartition> allTopicPartitions;

  private BackPressureManagerState state = new BackPressureManagerNormalState();

  public BackPressureManager(BackPressureConfig backPressureConfig, Set<TopicPartition> allTopicPartitions) {
    this.backPressureConfig = backPressureConfig;
    this.allTopicPartitions = allTopicPartitions;
  }

  public BackPressureActions update(int backlog) {
    BackPressureManagerStateAndActions stateAndActions = state.update(allTopicPartitions, backlog, backPressureConfig);
    this.state = stateAndActions.state;
    return stateAndActions.actions;
  }


}
