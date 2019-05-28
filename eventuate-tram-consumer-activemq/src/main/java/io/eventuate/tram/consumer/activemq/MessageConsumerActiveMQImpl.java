package io.eventuate.tram.consumer.activemq;

import io.eventuate.common.ChannelType;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.common.DecoratedMessageHandlerFactory;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.common.SubscriberIdAndMessage;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.ChannelType;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class MessageConsumerActiveMQImpl implements MessageConsumerImplementation {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private final String id = UUID.randomUUID().toString();

  @Autowired
  private DecoratedMessageHandlerFactory decoratedMessageHandlerFactory;

  private ActiveMQConnectionFactory connectionFactory;

  private Connection connection;
  private Session session;
  private List<javax.jms.MessageConsumer> consumers = new ArrayList<>();
  private List<Future<Void>> processingFutures = new ArrayList<>();
  private Map<String, ChannelType> messageModes;

  private AtomicBoolean runFlag = new AtomicBoolean(true);

  public MessageConsumerActiveMQImpl(String url,
                                     Optional<String> user,
                                     Optional<String> password) {
    this(url, Collections.emptyMap(), user, password);
  }

  public MessageConsumerActiveMQImpl(String url,
                                     Map<String, ChannelType> messageModes,
                                     Optional<String> user,
                                     Optional<String> password) {
    this.messageModes = messageModes;
    connectionFactory = createActiveMQConnectionFactory(url, user, password);

    try {
      connection = connectionFactory.createConnection();
      connection.setExceptionListener(e -> logger.error(e.getMessage(), e));
      connection.start();
      session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
    } catch (JMSException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    try {
      List<javax.jms.MessageConsumer> subscriptionConsumers = new ArrayList<>();
      for (String channel : channels) {
        ChannelType mode = messageModes.getOrDefault(channel, ChannelType.TOPIC);

        String destinationName = mode == ChannelType.TOPIC ?
                String.format("Consumer.%s.VirtualTopic.%s", formatSubscriberId(subscriberId), channel) :
                channel;

        Destination destination = session.createQueue(destinationName);

        javax.jms.MessageConsumer consumer = session.createConsumer(destination);
        consumers.add(consumer);
        subscriptionConsumers.add(consumer);

        Consumer<SubscriberIdAndMessage> decoratedHandler = decoratedMessageHandlerFactory.decorate(handler);

        processingFutures.add(CompletableFuture.supplyAsync(() -> process(subscriberId, consumer, decoratedHandler)));
      }
      return () -> {
        subscriptionConsumers.forEach(consumer -> {
          try {
            consumer.close();
          } catch (JMSException e) {
            throw new RuntimeException(e);
          }
        });
      };
    } catch (JMSException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private String formatSubscriberId(String subscriberId) {
    return subscriberId.replace(".", "::");
  }

  private ActiveMQConnectionFactory createActiveMQConnectionFactory(String url, Optional<String> user, Optional<String> password) {
    return user
            .flatMap(usr -> password.flatMap(pass ->
                    Optional.of(new ActiveMQConnectionFactory(usr, pass, url))))
            .orElseGet(() -> new ActiveMQConnectionFactory(url));
  }

  private Void process(String subscriberId,
                       javax.jms.MessageConsumer consumer,
                       Consumer<SubscriberIdAndMessage> decoratedHandler) {
    while (runFlag.get()) {
      try {
        javax.jms.Message message = consumer.receive(100);

        if (message == null) {
          continue;
        }

        TextMessage textMessage = (TextMessage) message;
        Message tramMessage = JSonMapper.fromJson(textMessage.getText(), MessageImpl.class);

        try {
          logger.trace("Invoking handler {} {}", subscriberId, tramMessage.getId());
          decoratedHandler.accept(new SubscriberIdAndMessage(subscriberId, tramMessage));
          logger.trace("handled message {} {}", subscriberId, tramMessage.getId());
        } catch (Throwable t) {
          logger.trace("Got exception {} {}", subscriberId, tramMessage.getId());
          logger.trace("Got exception ", t);
        } finally {
          acknowledge(textMessage);
        }

      } catch (JMSException e) {
        logger.error(e.getMessage(), e);
      }
    }

    try {
      consumer.close();
    } catch (JMSException e) {
      logger.error(e.getMessage(), e);
    }

    return null;
  }

  private void acknowledge(TextMessage textMessage) {
    try {
      textMessage.acknowledge();
    } catch (JMSException e) {
      logger.error(e.getMessage(), e);
    }
  }

  public void close() {
    runFlag.set(false);

    processingFutures.forEach(f -> {
      try {
        f.get();
      } catch (InterruptedException | ExecutionException e) {
        logger.error(e.getMessage(), e);
      }
    });

    try {
      session.close();
      connection.close();
    } catch (JMSException e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Override
  public String getId() {
    return id;
  }
}