package io.eventuate.tram.consumer.rabbitmq;

import com.rabbitmq.client.*;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.messaging.common.ChannelType;
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
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageConsumerRabbitMQImpl implements MessageConsumer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private DuplicateMessageDetector duplicateMessageDetector;

  private Map<String, ChannelType> messageModes;

  private Connection connection;
  private List<Channel> channels = new ArrayList<>();

  public MessageConsumerRabbitMQImpl(String url) {
    this(url, Collections.emptyMap());
  }

  public MessageConsumerRabbitMQImpl(String url, Map<String, ChannelType> messageModes) {
    this.messageModes = messageModes;

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(url);
    try {
      connection = factory.newConnection();
    } catch (IOException | TimeoutException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    try {
      for (String channelName : channels) {
        ChannelType channelType = messageModes.getOrDefault(channelName, ChannelType.TOPIC);

        String queueName;
        Channel channel = connection.createChannel();

        if (channelType == ChannelType.TOPIC) {
          channel.exchangeDeclare(channelName, "fanout");
          queueName = channel.queueDeclare().getQueue();
          channel.queueBind(queueName, channelName, "");
        } else {
          channel.queueDeclare(channelName, true, false, false, null);
          queueName = channelName;
        }

        channel.basicConsume(queueName, false, createConsumer(subscriberId, handler, channel));
        this.channels.add(channel);
      }
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private Consumer createConsumer(String subscriberId, MessageHandler handler, Channel channel) {
    return new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag,
                                 Envelope envelope,
                                 AMQP.BasicProperties properties,
                                 byte[] body) throws IOException {

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

  public void close() {
    try {
      connection.close();
      channels.forEach(channel -> {
        try {
          channel.close();
        } catch (IOException | TimeoutException e) {
          logger.error(e.getMessage(), e);
        }
      });
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }
}