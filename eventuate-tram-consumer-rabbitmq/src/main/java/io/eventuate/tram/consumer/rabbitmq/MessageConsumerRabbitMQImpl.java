package io.eventuate.tram.consumer.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.eventuate.tram.consumer.common.DecoratedMessageHandlerFactory;
import io.eventuate.tram.consumer.common.SubscriberIdAndMessage;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class MessageConsumerRabbitMQImpl implements MessageConsumer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  public final String id = UUID.randomUUID().toString();

  @Autowired
  private DecoratedMessageHandlerFactory decoratedMessageHandlerFactory;

  private Connection connection;
  private int partitionCount;
  private String zkUrl;

  private List<Subscription> subscriptions = new ArrayList<>();

  public MessageConsumerRabbitMQImpl(String rabbitMQUrl, String zkUrl, int partitionCount) {
    this.partitionCount = partitionCount;
    this.zkUrl = zkUrl;
    prepareRabbitMQConnection(rabbitMQUrl);

    logger.info("consumer {} created and ready to subscribe", id);
  }

  public void setSubscriptionLifecycleHook(SubscriptionLifecycleHook subscriptionLifecycleHook) {
    subscriptions.forEach(subscription -> subscription.setSubscriptionLifecycleHook(subscriptionLifecycleHook));
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
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    logger.info("consumer {} with subscriberId {} is subscribing to channels {}", id, subscriberId, channels);

    Consumer<SubscriberIdAndMessage> decoratedHandler = decoratedMessageHandlerFactory.decorate(handler);

    Subscription subscription = new Subscription(id,
            connection,
            zkUrl,
            subscriberId,
            channels,
            partitionCount,
            (message, acknowledgeCallback) -> handleMessage(subscriberId, decoratedHandler, message, acknowledgeCallback));

    subscriptions.add(subscription);

    logger.info("consumer {} with subscriberId {} subscribed to channels {}", id, subscriberId, channels);
    return () -> {
      subscription.close();
      subscriptions.remove(subscription);
    };
  }

  private void handleMessage(String subscriberId, Consumer<SubscriberIdAndMessage> decoratedHandler, Message tramMessage, Runnable acknowledgeCallback) {
    try {
      decoratedHandler.accept(new SubscriberIdAndMessage(subscriberId, tramMessage));
      logger.info("consumer {} with subscriberId {} handled message with id {}", id, subscriberId, tramMessage.getId());
    } catch (Throwable t) {
      logger.info("consumer {} with subscriberId {} got exception when tried to handle message with id {}", id, subscriberId, tramMessage.getId());
      logger.info("Got exception ", t);
    } finally {
      acknowledgeCallback.run();
    }
  }

  public void close() {
    logger.info("consumer {} is closing", id);

    subscriptions.forEach(Subscription::close);

    try {
      connection.close();
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }

    logger.info("consumer {} is closed", id);
  }
}