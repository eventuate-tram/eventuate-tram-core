package io.eventuate.messaging.kafka.basic.consumer;

import org.apache.kafka.common.TopicPartition;

import java.util.Set;

public interface BackPressureManagerState {
  BackPressureManagerStateAndActions update(Set<TopicPartition> allTopicPartitions, int backlog, BackPressureConfig backPressureConfig);
}
