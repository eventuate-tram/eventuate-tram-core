package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Subscription {
  private final String subscriptionId = UUID.randomUUID().toString();
  private String consumerId;
  private RedisTemplate<String, String> redisTemplate;
  private TransactionTemplate transactionTemplate;
  private DuplicateMessageDetector duplicateMessageDetector;
  private String subscriberId;
  private MessageHandler handler;
  private boolean acknowledgeFailedMessages;
  private ExecutorService executorService = Executors.newCachedThreadPool();
  private Coordinator coordinator;
  private Map<String, Set<Integer>> currentPartitionsByChannel = new HashMap<>();
  private Map<ChannelPartition, ChannelProcessor> channelProcessorsByChannelAndPartition = new HashMap<>();
  private Optional<SubscriptionLifecycleHook> subscriptionLifecycleHook = Optional.empty();

  public Subscription(String consumerId,
                      RedisTemplate<String, String> redisTemplate,
                      TransactionTemplate transactionTemplate,
                      DuplicateMessageDetector duplicateMessageDetector,
                      String subscriberId,
                      Set<String> channels,
                      MessageHandler handler,
                      Boolean acknowledgeFailedMessages,
                      int partitions) {

    this.consumerId = consumerId;
    this.redisTemplate = redisTemplate;
    this.transactionTemplate = transactionTemplate;
    this.duplicateMessageDetector = duplicateMessageDetector;
    this.subscriberId = subscriberId;
    this.handler = handler;
    this.acknowledgeFailedMessages = acknowledgeFailedMessages;

    coordinator = new Coordinator(subscriptionId,
            "172.17.0.1:2181",
            subscriberId,
            channels,
            this::assignmentUpdated,
            partitions);

    channels.forEach(channelName -> currentPartitionsByChannel.put(channelName, new HashSet<>()));
  }

  public void setSubscriptionLifecycleHook(SubscriptionLifecycleHook subscriptionLifecycleHook) {
    this.subscriptionLifecycleHook = Optional.ofNullable(subscriptionLifecycleHook);
  }

  private void assignmentUpdated(Assignment assignment) {

    for (String channelName : currentPartitionsByChannel.keySet()) {
      Set<Integer> currentPartitions = currentPartitionsByChannel.get(channelName);
      Set<Integer> expectedPartitions = assignment.getPartitionAssignmentsByChannel().get(channelName);

      Set<Integer> resignedPartitions = currentPartitions
              .stream()
              .filter(currentPartition -> !expectedPartitions.contains(currentPartition))
              .collect(Collectors.toSet());

      Set<Integer> assignedPartitions = expectedPartitions
              .stream()
              .filter(expectedPartition -> !currentPartitions.contains(expectedPartition))
              .collect(Collectors.toSet());

      resignedPartitions.forEach(resignedPartition -> {
        channelProcessorsByChannelAndPartition.get(new ChannelPartition(channelName, resignedPartition)).stop();
      });

      assignedPartitions.forEach(assignedPartition -> {
        ChannelProcessor channelProcessor = new ChannelProcessor(redisTemplate,
                transactionTemplate,
                duplicateMessageDetector,
                subscriberId,
                channelName + "-" + assignedPartition,
                handler,
                acknowledgeFailedMessages);

        executorService.submit(channelProcessor::process);

        channelProcessorsByChannelAndPartition.put(new ChannelPartition(channelName, assignedPartition), channelProcessor);
      });

      currentPartitionsByChannel.put(channelName, expectedPartitions);

      subscriptionLifecycleHook.ifPresent(sh -> sh.partitionsUpdated(channelName, subscriptionId, expectedPartitions));
    }
  }

  public void close() {
    coordinator.close();
    channelProcessorsByChannelAndPartition.values().forEach(ChannelProcessor::stop);
  }

  private static class ChannelPartition {
    private String channel;
    private int partition;

    public ChannelPartition() {
    }

    public ChannelPartition(String channel, int partition) {
      this.channel = channel;
      this.partition = partition;
    }

    public String getChannel() {
      return channel;
    }

    public void setChannel(String channel) {
      this.channel = channel;
    }

    public int getPartition() {
      return partition;
    }

    public void setPartition(int partition) {
      this.partition = partition;
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }
  }
}
