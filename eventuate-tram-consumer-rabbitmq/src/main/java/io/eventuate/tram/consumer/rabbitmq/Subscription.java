package io.eventuate.tram.consumer.rabbitmq;

import com.rabbitmq.client.*;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Subscription {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private final String subscriptionId = UUID.randomUUID().toString();

  private Connection connection;
  private String subscriberId;
  private Set<String> channels;
  private int partitionCount;
  private BiConsumer<Message, Runnable> handleMessageCallback;

  private Channel consumerChannel;
  private Map<String, String> consumerTagByQueue = new HashMap<>();
  private Coordinator coordinator;
  private Map<String, Set<Integer>> currentPartitionsByChannel = new HashMap<>();
  private Channel subscriberGroupChannel;

  public Subscription(Connection connection,
                      String zkUrl,
                      String subscriberId,
                      Set<String> channels,
                      int partitionCount,
                      BiConsumer<Message, Runnable> handleMessageCallback) {

    this.connection = connection;
    this.subscriberId = subscriberId;
    this.channels = channels;
    this.partitionCount = partitionCount;
    this.handleMessageCallback = handleMessageCallback;

    channels.forEach(channelName -> currentPartitionsByChannel.put(channelName, new HashSet<>()));

    PartitionManager partitionManager = createPartitionManager(partitionCount);

    coordinator = createCoordinator(subscriptionId,
            zkUrl,
            subscriberId,
            channels,
            this::leaderSelected,
            this::leaderRemoved,
            this::assignmentUpdated,
            partitionManager::rebalance);

    consumerChannel = createRabbitMQChannel();
  }

  protected PartitionManager createPartitionManager(int partitionCount) {
    return new PartitionManager(partitionCount);
  }

  protected Coordinator createCoordinator(String groupMemberId,
                                          String zkUrl,
                                          String subscriberId,
                                          Set<String> channels,
                                          Runnable leaderSelectedCallback,
                                          Runnable leaderRemovedCallback,
                                          java.util.function.Consumer<Assignment> assignmentUpdatedCallback,
                                          java.util.function.Consumer<Map<String, Assignment>> manageAssignmentsCallback) {

    return new Coordinator(groupMemberId,
            zkUrl,
            subscriberId,
            channels,
            leaderSelectedCallback,
            leaderRemovedCallback,
            assignmentUpdatedCallback,
            partitionCount);
  }

  public void close() {
    coordinator.close();

    try {
      consumerChannel.close();
    } catch (IOException | TimeoutException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private Channel createRabbitMQChannel() {
    try {
      return connection.createChannel();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void leaderSelected() {
    subscriberGroupChannel = createRabbitMQChannel();

    for (String channelName : channels) {
      try {
        subscriberGroupChannel.exchangeDeclare(makeConsistentHashExchangeName(channelName, subscriberId), "x-consistent-hash");

        for (int i = 0; i < partitionCount; i++) {
          subscriberGroupChannel.queueDeclare(makeConsistentHashQueueName(channelName, subscriberId, i), true, false, false, null);
          subscriberGroupChannel.queueBind(makeConsistentHashQueueName(channelName, subscriberId, i), makeConsistentHashExchangeName(channelName, subscriberId), "10");
        }

        subscriberGroupChannel.exchangeDeclare(channelName, "fanout");
        subscriberGroupChannel.exchangeBind(makeConsistentHashExchangeName(channelName, subscriberId), channelName, "");

      } catch (IOException e) {
        logger.error(e.getMessage(), e);
        throw new RuntimeException(e);
      }
    }
  }

  private void leaderRemoved() {
    try {
      subscriberGroupChannel.close();
    } catch (IOException | TimeoutException e) {
      logger.error(e.getMessage(), e);
    }
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
        try {
          String queue = makeConsistentHashQueueName(channelName, subscriberId, resignedPartition);
          consumerChannel.basicCancel(consumerTagByQueue.remove(queue));
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
          throw new RuntimeException(e);
        }
      });

      assignedPartitions.forEach(assignedPartition -> {
        try {
          String queue = makeConsistentHashQueueName(channelName, subscriberId, assignedPartition);
          String exchange = makeConsistentHashExchangeName(channelName, subscriberId);

          consumerChannel.exchangeDeclare(exchange, "x-consistent-hash");
          consumerChannel.queueDeclare(queue, true, false, false, null);
          consumerChannel.queueBind(queue, exchange, "10");

          String tag = consumerChannel.basicConsume(queue, false, createConsumer(queue));

          consumerTagByQueue.put(queue, tag);
        } catch (IOException e) {
          logger.error(e.getMessage(), e);
          throw new RuntimeException(e);
        }
      });
    }
  }

  private Consumer createConsumer(String queueName) {
    return new DefaultConsumer(consumerChannel) {
      @Override
      public void handleDelivery(String consumerTag,
                                 Envelope envelope,
                                 AMQP.BasicProperties properties,
                                 byte[] body) throws IOException {
        logger.info("Got message from queue {}", queueName);

        String message = new String(body, "UTF-8");
        Message tramMessage = JSonMapper.fromJson(message, MessageImpl.class);
        handleMessageCallback.accept(tramMessage, () -> acknowledge(envelope, consumerChannel));
      }
    };
  }

  private void acknowledge(Envelope envelope, Channel channel) {
    try {
      channel.basicAck(envelope.getDeliveryTag(), false);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private String makeConsistentHashExchangeName(String channelName, String subscriberId) {
    return String.format("%s-%s", channelName, subscriberId);
  }

  private String makeConsistentHashQueueName(String channelName, String subscriberId, int partition) {
    return String.format("%s-%s-%s", channelName, subscriberId, partition);
  }
}
