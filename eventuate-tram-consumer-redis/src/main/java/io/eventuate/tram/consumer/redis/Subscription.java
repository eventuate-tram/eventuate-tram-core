package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Subscription {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private final String subscriptionId;
  private String consumerId;
  private RedisTemplate<String, String> redisTemplate;
  private TransactionTemplate transactionTemplate;
  private DuplicateMessageDetector duplicateMessageDetector;
  private String subscriberId;
  private MessageHandler handler;
  private ExecutorService executorService = Executors.newCachedThreadPool();
  private Coordinator coordinator;
  private Map<String, Set<Integer>> currentPartitionsByChannel = new HashMap<>();
  private ConcurrentHashMap<ChannelPartition, ChannelProcessor> channelProcessorsByChannelAndPartition = new ConcurrentHashMap<>();
  private Optional<SubscriptionLifecycleHook> subscriptionLifecycleHook = Optional.empty();

  public Subscription(String subscribtionId,
                      String consumerId,
                      RedisTemplate<String, String> redisTemplate,
                      RedissonClient redissonClient,
                      TransactionTemplate transactionTemplate,
                      DuplicateMessageDetector duplicateMessageDetector,
                      String subscriberId,
                      Set<String> channels,
                      MessageHandler handler,
                      int partitions,
                      long groupMemberTtlInMilliseconds,
                      long listenerIntervalInMilliseconds,
                      long assignmentTtlInMilliseconds,
                      long leadershipTtlInMilliseconds) {

    this.subscriptionId = subscribtionId;
    this.consumerId = consumerId;
    this.redisTemplate = redisTemplate;
    this.transactionTemplate = transactionTemplate;
    this.duplicateMessageDetector = duplicateMessageDetector;
    this.subscriberId = subscriberId;
    this.handler = handler;

    channels.forEach(channelName -> currentPartitionsByChannel.put(channelName, new HashSet<>()));

    coordinator = new Coordinator(redisTemplate,
            redissonClient,
            subscriptionId,
            subscriberId,
            channels,
            this::assignmentUpdated,
            partitions,
            groupMemberTtlInMilliseconds,
            listenerIntervalInMilliseconds,
            assignmentTtlInMilliseconds,
            leadershipTtlInMilliseconds);


    logger.info("subscription created (channels = {}, {})", channels, identificationInformation());
  }

  public void setSubscriptionLifecycleHook(SubscriptionLifecycleHook subscriptionLifecycleHook) {
    this.subscriptionLifecycleHook = Optional.ofNullable(subscriptionLifecycleHook);
  }

  private void assignmentUpdated(Assignment assignment) {

    logger.info("assignment is updated (assignment = {}, {})", assignment, identificationInformation());

    for (String channelName : currentPartitionsByChannel.keySet()) {
      Set<Integer> currentPartitions = currentPartitionsByChannel.get(channelName);
      Set<Integer> expectedPartitions = assignment.getPartitionAssignmentsByChannel().get(channelName);

      Set<Integer> resignedPartitions = currentPartitions
              .stream()
              .filter(currentPartition -> !expectedPartitions.contains(currentPartition))
              .collect(Collectors.toSet());

      logger.info("partitions resigned (resigned partitions = {}, {})", resignedPartitions, identificationInformation());

      Set<Integer> assignedPartitions = expectedPartitions
              .stream()
              .filter(expectedPartition -> !currentPartitions.contains(expectedPartition))
              .collect(Collectors.toSet());

      logger.info("partitions asigned (resigned partitions = {}, {})", assignment, identificationInformation());


      resignedPartitions.forEach(resignedPartition ->
        channelProcessorsByChannelAndPartition.remove(new ChannelPartition(channelName, resignedPartition)).stop());

      assignedPartitions.forEach(assignedPartition -> {
        ChannelProcessor channelProcessor = new ChannelProcessor(redisTemplate,
                transactionTemplate,
                duplicateMessageDetector,
                subscriberId,
                channelName + "-" + assignedPartition,
                handler,
                identificationInformation());

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

  private String identificationInformation() {
    return String.format("(consumerId = %s, subscriptionId = %s, subscriberId = %s)", consumerId, subscriptionId, subscriberId);
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
