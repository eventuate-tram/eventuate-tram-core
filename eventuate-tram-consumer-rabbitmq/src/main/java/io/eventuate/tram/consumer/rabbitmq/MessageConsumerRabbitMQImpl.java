package io.eventuate.tram.consumer.rabbitmq;

import com.rabbitmq.client.*;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class MessageConsumerRabbitMQImpl implements MessageConsumer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private DuplicateMessageDetector duplicateMessageDetector;

  private Connection connection;
  private List<Channel> channels = new ArrayList<>();
  private int partitionCount;
  private List<Coordinator> coordinators = new ArrayList<>();
  private String zkUrl;
  private PartitionManager partitionManager;

  public MessageConsumerRabbitMQImpl(String rabbitMQUrl, String zkUrl, int partitionCount) {
    this.partitionCount = partitionCount;
    partitionManager = new PartitionManager(partitionCount);
    this.zkUrl = zkUrl;
    prepareRabbitMQConnection(rabbitMQUrl);
  }

  private void prepareRabbitMQConnection(String rabbitMQUrl) {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(rabbitMQUrl);
    try {
      connection = factory.newConnection();
    } catch (IOException | TimeoutException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {

      Channel consumerChannel = createRabbitMQChannel();
      this.channels.add(consumerChannel);

      Channel leaderChannel = createRabbitMQChannel();

      Coordinator coordinator = new Coordinator(zkUrl,
              subscriberId,
              channels,
              () -> leaderSelected(leaderChannel, channels, subscriberId),
              () -> leaderRemoved(leaderChannel),
              assignment -> assignmentUpdated(consumerChannel, subscriberId, assignment, handler),
              partitionManager::rebalance/*this::manageAssignments*/);

      coordinators.add(coordinator);
  }

  private void leaderSelected(Channel leaderChannel, Set<String> channels, String subscriberId) {
    for (String channelName : channels) {
      try {
        leaderChannel.exchangeDeclare(makeConsistentHashExchangeName(channelName, subscriberId), "x-consistent-hash");

        for (int i = 0; i < partitionCount; i++) {
          leaderChannel.queueDeclare(makeConsistentHashQueueName(channelName, subscriberId, i), true, false, false, null);
          leaderChannel.queueBind(makeConsistentHashQueueName(channelName, subscriberId, i), makeConsistentHashExchangeName(channelName, subscriberId), "10");
        }

        leaderChannel.exchangeDeclare(channelName, "fanout");
        String fanoutQueueName = leaderChannel.queueDeclare().getQueue();
        leaderChannel.queueBind(fanoutQueueName, channelName, "");

        leaderChannel.basicConsume(fanoutQueueName, true, new DefaultConsumer(leaderChannel) {
          @Override
          public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

            for (int i = 0; i < partitionCount; i++) {
              leaderChannel.queueDeclare(makeConsistentHashQueueName(channelName, subscriberId, i), true, false, false, null);
              leaderChannel.queueBind(makeConsistentHashQueueName(channelName, subscriberId, i), makeConsistentHashExchangeName(channelName, subscriberId), "10");
            }

            leaderChannel.basicPublish(makeConsistentHashExchangeName(channelName, subscriberId),
                    properties.getHeaders().get("key").toString(),
                    null,
                    body);
          }
        });

      } catch (IOException e) {
        logger.error(e.getMessage(), e);
      }
    }
  }

  private void leaderRemoved(Channel leaderChannel) {
    try {
      leaderChannel.close();
    } catch (IOException | TimeoutException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private void assignmentUpdated(Channel channel, String subscriberId, Assignment assignment, MessageHandler messageHandler) {
    String channelName = assignment.getChannelName();

    assignment.getResignedPartitions().forEach(partition -> {
      try {
        channel.queueUnbind(makeConsistentHashQueueName(channelName, subscriberId, partition),
                makeConsistentHashExchangeName(channelName, subscriberId),
                "10");

        assignment.getCurrentPartitions().remove(partition);
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        throw new RuntimeException(e);
      }
    });

    assignment.setResignedPartitions(Collections.emptySet());

    assignment.getAssignedPartitions().forEach(partition -> {
      try {
        channel.queueBind(makeConsistentHashQueueName(channelName, subscriberId, partition),
                makeConsistentHashExchangeName(channelName, subscriberId),
                "10");

        assignment.getCurrentPartitions().add(partition);

        channel.basicConsume(makeConsistentHashQueueName(channelName, subscriberId, partition),
                false,
                createConsumer(subscriberId, messageHandler, channel, makeConsistentHashQueueName(channelName, subscriberId, partition)));

      } catch (IOException e) {
        logger.error(e.getMessage(), e);
        throw new RuntimeException(e);
      }
    });

    assignment.setAssignedPartitions(Collections.emptySet());

    assignment.setState(AssignmentState.NORMAL);
  }

//  private void manageAssignments(List<Assignment> assignments) {
//    int subscribers = assignments.size() > partitionCount ? partitionCount : assignments.size();
//
//    int partitionCounter = 0;
//    for (int i = 0; i < subscribers - 1; i++) {
//      Set<Integer> partitionsToAssign = new HashSet<>();
//
//      for (int j = 0; j < partitionCount / subscribers; j++) {
//        partitionsToAssign.add(partitionCounter++);
//      }
//
//      assignments.get(i).setAssignedPartitions(partitionsToAssign);
//    }
//
//    if (subscribers - 1 >= 0) {
//      Set<Integer> queuesToAssign = new HashSet<>();
//      for (int i = 0; i < partitionCount /subscribers + partitionCount % subscribers; i++) {
//        queuesToAssign.add(partitionCounter++);
//      }
//      assignments.get(subscribers - 1).setAssignedPartitions(queuesToAssign);
//    }
//
//    for (int i = partitionCount; i < assignments.size(); i++) {
//      assignments.get(i).setResignedPartitions(assignments.get(i).getCurrentPartitions());
//    }
//
//    assignments.forEach(assignment -> assignment.setState(AssignmentState.REBALANSING));
//  }

  private Channel createRabbitMQChannel() {
    try {
      return connection.createChannel();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Consumer createConsumer(String subscriberId, MessageHandler handler, Channel channel, String queueName) {
    return new DefaultConsumer(channel) {

      @Override
      public void handleDelivery(String consumerTag,
                                 Envelope envelope,
                                 AMQP.BasicProperties properties,
                                 byte[] body) throws IOException {

        logger.info("Got message from queue {}", queueName);

        String message = new String(body, "UTF-8");
        Message tramMessage = JSonMapper.fromJson(message, MessageImpl.class);

        transactionTemplate.execute(ts -> {
          if (duplicateMessageDetector.isDuplicate(subscriberId, tramMessage.getId())) {
            logger.trace("Duplicate message {} {}", subscriberId, tramMessage.getId());
            acknowledge(envelope, channel);
            return null;
          }

          try {
            logger.trace("Invoking handler {} {}", subscriberId, tramMessage.getId());
            handler.accept(tramMessage);
            logger.trace("handled message {} {}", subscriberId, tramMessage.getId());
          } catch (Throwable t) {
            logger.trace("Got exception {} {}", subscriberId, tramMessage.getId());
            logger.trace("Got exception ", t);
          } finally {
            acknowledge(envelope, channel);
          }

          return null;
        });
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

  public void close() {
    coordinators.forEach(Coordinator::close);

    channels.forEach(channel -> {
      try {
        channel.close();
      } catch (IOException | TimeoutException e) {
        logger.error(e.getMessage(), e);
      }
    });

    try {
      connection.close();
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }
}