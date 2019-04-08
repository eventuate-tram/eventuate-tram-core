package io.eventuate.tram.inmemory;


import io.eventuate.javaclient.spring.jdbc.IdGenerator;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import io.eventuate.tram.messaging.producer.AbstractMessageProducer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.messaging.producer.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.util.Collections.singleton;

public class InMemoryMessaging extends AbstractMessageProducer implements MessageProducer, MessageConsumer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private IdGenerator idGenerator;

  @Autowired
  private TransactionTemplate transactionTemplate;

  private Executor executor = Executors.newCachedThreadPool();

  private ConcurrentHashMap<String, List<MessageHandlerWithSubscriberId>> subscriptions = new ConcurrentHashMap<>();
  private List<MessageHandlerWithSubscriberId> wildcardSubscriptions = new ArrayList<>();

  protected InMemoryMessaging(MessageInterceptor[] messageInterceptors) {
    super(messageInterceptors);
  }

  @Override
  public void send(String destination, Message message) {
    String id = idGenerator.genId().asString();
    message.getHeaders().put(Message.ID, id);
    if (TransactionSynchronizationManager.isActualTransactionActive()) {
      logger.info("Transaction active");
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
        @Override
        public void afterCommit() {
          reallySend(destination, message);
        }
      });
    } else {
      logger.info("No transaction active");
      reallySend(destination, message);
    }

  }

  private void reallySend(String destination, Message message) {
    sendMessage(null, destination, message, this::send);
  }

  private void send(Message message) {
    String destination = message.getRequiredHeader(Message.DESTINATION);
    List<MessageHandlerWithSubscriberId> handlers = subscriptions.getOrDefault(destination, Collections.emptyList());
    sendToHandlers(destination, message, handlers);
    sendToHandlers(destination, message, wildcardSubscriptions);
  }

  private void sendToHandlers(String destination, Message message, List<MessageHandlerWithSubscriberId> handlers) {
    logger.info("sending to channel {} that has {} subscriptions this message {} ", destination, handlers.size(), message);
    preReceive(message);
    for (MessageHandlerWithSubscriberId handler : handlers) {
      executor.execute(() -> transactionTemplate.execute(ts -> {
        try {
          preHandle(handler.getSubscriber(), message);
          handler.getMessageHandler().accept(message);
          postHandle(handler.getSubscriber(), message, null);
        } catch (Throwable t) {
          postHandle(handler.getSubscriber(), message, t);
          logger.error("message handler " + destination, t);
        }
        return null;
      }));
      postReceive(message);
    }
  }

  private void postHandle(String subscriberId, Message message, Throwable t) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.postHandle(subscriberId, message, t));
  }

  private void preHandle(String subscriberId, Message message) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.preHandle(subscriberId, message));
  }

  private void preReceive(Message message) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.preReceive(message));
  }

  private void postReceive(Message message) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.postReceive(message));
  }

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    MessageHandlerWithSubscriberId mh = new MessageHandlerWithSubscriberId(subscriberId, handler);
    if (singleton("*").equals(channels)) {
      logger.info("subscribing {} to wildcard channels", subscriberId);
      wildcardSubscriptions.add(mh);
    } else {
      logger.info("subscribing {} to channels {}", subscriberId, channels);
      for (String channel : channels) {
        List<MessageHandlerWithSubscriberId> handlers = subscriptions.computeIfAbsent(channel, k -> new ArrayList<>());
        handlers.add(mh);
      }
    }
    return () -> {
      wildcardSubscriptions.remove(mh);
      for (String channel : channels) {
        subscriptions.get(channel).remove(mh);
      }
    };
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public void close() {
  }
}
