package io.eventuate.tram.consumer.rabbitmq;

import java.util.*;

public class Assignment {
  private Set<String> channels;
  private Map<String, Set<Integer>> partitionAssignmentsByChannel;

  public Assignment() {
  }

  public Assignment(Assignment copy) {
    this.channels = new HashSet<>(copy.getChannels());
    this.partitionAssignmentsByChannel = new HashMap<>();
    copy.getPartitionAssignmentsByChannel().forEach((channel, partitions) -> partitionAssignmentsByChannel.put(channel, new HashSet<>(partitions)));
  }

  public Assignment(Set<String> channels, Map<String, Set<Integer>> partitionAssignmentsByChannel) {
    this.channels = channels;
    this.partitionAssignmentsByChannel = partitionAssignmentsByChannel;
  }

  public Set<String> getChannels() {
    return channels;
  }

  public void setChannels(Set<String> channels) {
    this.channels = channels;
  }

  public Map<String, Set<Integer>> getPartitionAssignmentsByChannel() {
    return partitionAssignmentsByChannel;
  }

  public void setPartitionAssignmentsByChannel(Map<String, Set<Integer>> partitionAssignmentsByChannel) {
    this.partitionAssignmentsByChannel = partitionAssignmentsByChannel;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Assignment that = (Assignment) o;

    return Objects.equals(channels, that.channels) &&
            Objects.equals(partitionAssignmentsByChannel, that.partitionAssignmentsByChannel);
  }

  @Override
  public int hashCode() {
    return Objects.hash(channels, partitionAssignmentsByChannel);
  }
}
