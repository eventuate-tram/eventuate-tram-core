package io.eventuate.tram.consumer.rabbitmq;

import com.rabbitmq.client.*;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.common.coordinator.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Subscription {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private final String subscriptionId;
  private final String consumerId;

  private CoordinatorFactory coordinatorFactory;
  private Connection connection;
  private String subscriberId;
  private Set<String> channels;
  private int partitionCount;
  private BiConsumer<Message, Runnable> handleMessageCallback;

  private Channel consumerChannel;
  private Map<String, String> consumerTagByQueue = new HashMap<>();
  private Coordinator coordinator;
  private Map<String, Set<Integer>> currentPartitionsByChannel = new HashMap<>();
  private Optional<SubscriptionLifecycleHook> subscriptionLifecycleHook = Optional.empty();
  private Optional<SubscriptionLeaderHook> leaderHook = Optional.empty();

  public Subscription(CoordinatorFactory coordinatorFactory,
                      String consumerId,
                      String subscriptionId,
                      Connection connection,
                      String subscriberId,
                      Set<String> channels,
                      int partitionCount,
                      BiConsumer<Message, Runnable> handleMessageCallback) {

    logger.info("Creating subscription for channels {} and partition count {}. {}",
            channels, partitionCount, identificationInformation());

    this.coordinatorFactory = coordinatorFactory;
    this.consumerId = consumerId;
    this.subscriptionId = subscriptionId;
    this.connection = connection;
    this.subscriberId = subscriberId;
    this.channels = channels;
    this.partitionCount = partitionCount;
    this.handleMessageCallback = handleMessageCallback;

    channels.forEach(channelName -> currentPartitionsByChannel.put(channelName, new HashSet<>()));

    consumerChannel = createRabbitMQChannel();

    coordinator = createCoordinator(
            subscriptionId,
            subscriberId,
            channels,
            this::leaderSelected,
            this::leaderRemoved,
            this::assignmentUpdated);

    logger.info("Created subscription for channels {} and partition count {}. {}",
            channels, partitionCount, identificationInformation());
  }

  public void setSubscriptionLifecycleHook(SubscriptionLifecycleHook subscriptionLifecycleHook) {
    this.subscriptionLifecycleHook = Optional.ofNullable(subscriptionLifecycleHook);
  }

  public void setLeaderHook(SubscriptionLeaderHook leaderHook) {
    this.leaderHook = Optional.ofNullable(leaderHook);
  }

  protected Coordinator createCoordinator(String groupMemberId,
                                          String subscriberId,
                                          Set<String> channels,
                                          Runnable leaderSelectedCallback,
                                          Runnable leaderRemovedCallback,
                                          java.util.function.Consumer<Assignment> assignmentUpdatedCallback) {

    return coordinatorFactory.makeCoordinator(subscriberId,
            channels,
            subscriptionId,
            assignmentUpdatedCallback,
            String.format("/eventuate-tram/rabbitmq/consumer-leaders/%s", subscriberId),
            leaderSelectedCallback,
            leaderRemovedCallback);
  }


  public void close() {
    logger.info("Closing subscription. {}", identificationInformation());

    coordinator.close();

    try {
      consumerChannel.close();
    } catch (IOException | TimeoutException e) {
      logger.error(e.getMessage(), e);
    }

    logger.info("Subscription is closed. {}", identificationInformation());
  }

  private Channel createRabbitMQChannel() {
    try {
      return connection.createChannel();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void leaderSelected() {
    leaderHook.ifPresent(hook -> hook.leaderUpdated(true, subscriptionId));
    logger.info("Subscription selected as leader. {}", identificationInformation());

    Channel subscriberGroupChannel = createRabbitMQChannel();

    for (String channelName : channels) {
      try {
        logger.info("Leading subscription is creating exchanges and queues for channel {}. {}",
                channelName, identificationInformation());

        subscriberGroupChannel.exchangeDeclare(makeConsistentHashExchangeName(channelName, subscriberId), "x-consistent-hash");

        for (int i = 0; i < partitionCount; i++) {
          subscriberGroupChannel.queueDeclare(makeConsistentHashQueueName(channelName, subscriberId, i), true, false, false, null);
          subscriberGroupChannel.queueBind(makeConsistentHashQueueName(channelName, subscriberId, i), makeConsistentHashExchangeName(channelName, subscriberId), "10");
        }

        subscriberGroupChannel.exchangeDeclare(channelName, "fanout");
        subscriberGroupChannel.exchangeBind(makeConsistentHashExchangeName(channelName, subscriberId), channelName, "");

        logger.info("Leading subscription created exchanges and queues for channel {}. {}",
                channelName, identificationInformation());
      } catch (IOException e) {
        logger.error(e.getMessage(), e);
        throw new RuntimeException(e);
      }
    }

    try {
      subscriberGroupChannel.close();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  private void leaderRemoved() {
    logger.info("Revoking leadership from subscription. {}", identificationInformation());
    leaderHook.ifPresent(hook -> hook.leaderUpdated(false, subscriptionId));
    logger.info("Leadership is revoked from subscription. {}", identificationInformation());
  }

  private void assignmentUpdated(Assignment assignment) {
    logger.info("Updating assignment {}. {} ", assignment, identificationInformation());

    for (String channelName : currentPartitionsByChannel.keySet()) {
      Set<Integer> currentPartitions = currentPartitionsByChannel.get(channelName);

      logger.info("Current partitions {} for channel {}. {}", currentPartitions, channelName, identificationInformation());

      Set<Integer> expectedPartitions = assignment.getPartitionAssignmentsByChannel().get(channelName);

      logger.info("Expected partitions {} for channel {}. {}", expectedPartitions, channelName, identificationInformation());

      Set<Integer> resignedPartitions = currentPartitions
              .stream()
              .filter(currentPartition -> !expectedPartitions.contains(currentPartition))
              .collect(Collectors.toSet());

      logger.info("Resigned partitions {} for channel {}. {}", resignedPartitions, channelName, identificationInformation());

      Set<Integer> assignedPartitions = expectedPartitions
              .stream()
              .filter(expectedPartition -> !currentPartitions.contains(expectedPartition))
              .collect(Collectors.toSet());

      logger.info("Assigned partitions {} for channel {}. {}", assignedPartitions, channelName, identificationInformation());

      resignedPartitions.forEach(resignedPartition -> {
        try {
          logger.info("Removing partition {} for channel {}. {}", resignedPartition, channelName, identificationInformation());

          String queue = makeConsistentHashQueueName(channelName, subscriberId, resignedPartition);
          consumerChannel.basicCancel(consumerTagByQueue.remove(queue));

          logger.info("Partition {} is removed for channel {}. {}", resignedPartition, channelName, identificationInformation());
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
          throw new RuntimeException(e);
        }
      });

      assignedPartitions.forEach(assignedPartition -> {
        try {
          logger.info("Assigning partition {} for channel {}. {}", assignedPartition, channelName, identificationInformation());


          String queue = makeConsistentHashQueueName(channelName, subscriberId, assignedPartition);
          String exchange = makeConsistentHashExchangeName(channelName, subscriberId);

          consumerChannel.exchangeDeclare(exchange, "x-consistent-hash");
          consumerChannel.queueDeclare(queue, true, false, false, null);
          consumerChannel.queueBind(queue, exchange, "10");

          String tag = consumerChannel.basicConsume(queue, false, createConsumer());

          consumerTagByQueue.put(queue, tag);

          logger.info("Partition {} is assigned for channel {}. {}", assignedPartition, channelName, identificationInformation());
        } catch (IOException e) {
          logger.error(e.getMessage(), e);
          throw new RuntimeException(e);
        }
      });

      currentPartitionsByChannel.put(channelName, expectedPartitions);

      subscriptionLifecycleHook.ifPresent(sh -> sh.partitionsUpdated(channelName, subscriptionId, expectedPartitions));
    }

    logger.info("assignment {} is updated. {}", assignment, identificationInformation());
  }

  private Consumer createConsumer() {
    return new DefaultConsumer(consumerChannel) {
      @Override
      public void handleDelivery(String consumerTag,
                                 Envelope envelope,
                                 AMQP.BasicProperties properties,
                                 byte[] body) throws IOException {
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

  private String identificationInformation() {
    return String.format("(consumerId = [%s], subscriptionId = [%s], subscriberId = [%s])", consumerId, subscriptionId, subscriberId);
  }
}
