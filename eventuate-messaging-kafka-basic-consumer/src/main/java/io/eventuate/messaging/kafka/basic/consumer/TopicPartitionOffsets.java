package io.eventuate.messaging.kafka.basic.consumer;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tracks the offsets for a TopicPartition that are being processed or have been processed
 */
public class TopicPartitionOffsets {

  /**
   * offsets that are being processed
   */
  private SortedSet<Long> unprocessed = new TreeSet<>();

  /**
   * offsets that have been processed
   */

  private Set<Long> processed = new HashSet<>();

  @Override
  public String toString() {
    return new ToStringBuilder(this)
            .append("unprocessed", unprocessed)
            .append("processed", processed)
            .toString();
  }

  public void noteUnprocessed(long offset) {
    unprocessed.add(offset);
  }

  public void noteProcessed(long offset) {
    processed.add(offset);
  }

  /**
   * @return large of all offsets that have been processed and can be committed
   */
  Optional<Long> offsetToCommit() {
    Optional<Long> result = Optional.empty();
    for (long x : unprocessed) {
      if (processed.contains(x))
        result = Optional.of(x);
      else
        break;
    }
    return result;
  }

  public void noteOffsetCommitted(long offset) {
    unprocessed = new TreeSet<>(unprocessed.stream().filter(x -> x >= offset).collect(Collectors.toList()));
    processed = processed.stream().filter(x -> x >= offset).collect(Collectors.toSet());
  }

  public Set<Long> getPending() {
    Set<Long> result = new HashSet<>(unprocessed);
    result.removeAll(processed);
    return result;
  }
}
