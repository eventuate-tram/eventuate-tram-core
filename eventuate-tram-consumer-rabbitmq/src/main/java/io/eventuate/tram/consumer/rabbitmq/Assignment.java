package io.eventuate.tram.consumer.rabbitmq;

import java.util.*;

public class Assignment {
  private Set<String> channels;
  private Map<String, Set<Integer>> partitionAssignmentsByChannel;

  public Assignment() {
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
}
