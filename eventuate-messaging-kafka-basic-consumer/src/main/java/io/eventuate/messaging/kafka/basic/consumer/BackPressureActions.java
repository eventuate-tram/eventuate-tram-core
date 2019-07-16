package io.eventuate.messaging.kafka.basic.consumer;

import org.apache.kafka.common.TopicPartition;

import java.util.Collections;
import java.util.Set;

public class BackPressureActions {

  public final Set<TopicPartition> pause;
  public final Set<TopicPartition> resume;

  public BackPressureActions(Set<TopicPartition> pause, Set<TopicPartition> resume) {
    this.pause = pause;
    this.resume = resume;
  }

  public static final BackPressureActions NONE = new BackPressureActions(Collections.emptySet(), Collections.emptySet());

  public static BackPressureActions pause(Set<TopicPartition> topicPartitions) {
    return new BackPressureActions(topicPartitions, Collections.emptySet());
  }

  public static BackPressureActions resume(Set<TopicPartition> topicPartitions) {
    return new BackPressureActions(Collections.emptySet(), topicPartitions);
  }
}
