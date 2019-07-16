package io.eventuate.messaging.kafka.basic.consumer;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Keeps track of message offsets that are (a) being processed and (b) have been processed and can be committed
 */
public class OffsetTracker {

  private Map<TopicPartition, TopicPartitionOffsets> state = new HashMap<>();

  @Override
  public String toString() {
    return new ToStringBuilder(this)
            .append("state", state)
            .toString();
  }

  TopicPartitionOffsets fetch(TopicPartition topicPartition) {
    TopicPartitionOffsets tpo = state.get(topicPartition);
    if (tpo == null) {
      tpo = new TopicPartitionOffsets();
      state.put(topicPartition, tpo);
    }
    return tpo;
  }
  void noteUnprocessed(TopicPartition topicPartition, long offset) {
    fetch(topicPartition).noteUnprocessed(offset);
  }

  void noteProcessed(TopicPartition topicPartition, long offset) {
    fetch(topicPartition).noteProcessed(offset);
  }

  public Map<TopicPartition, OffsetAndMetadata> offsetsToCommit() {
    Map<TopicPartition, OffsetAndMetadata> result = new HashMap<>();
    state.forEach((tp, tpo) -> {
      tpo.offsetToCommit().ifPresent(offset -> {
        result.put(tp, new OffsetAndMetadata(offset + 1, ""));
      });
    });
    return result;
  }

  public void noteOffsetsCommitted(Map<TopicPartition, OffsetAndMetadata> offsetsToCommit) {
    offsetsToCommit.forEach((tp, om) -> {
      fetch(tp).noteOffsetCommitted(om.offset());
    });
  }

  public Map<TopicPartition, Set<Long>> getPending() {
    Map<TopicPartition, Set<Long>> result = new HashMap<>();
    state.forEach((tp, tpo) -> {
      Set<Long> pending = tpo.getPending();
      if (!pending.isEmpty())
        result.put(tp, pending);
    });
    return result;
  }
}
