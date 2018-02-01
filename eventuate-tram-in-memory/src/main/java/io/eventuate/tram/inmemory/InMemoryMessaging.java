package io.eventuate.tram.inmemory;


import io.eventuate.javaclient.spring.jdbc.IdGenerator;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.util.Collections.singleton;

public class InMemoryMessaging implements MessageProducer, MessageConsumer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private IdGenerator idGenerator;

  @Autowired
  private TransactionTemplate transactionTemplate;

  private Executor executor = Executors.newCachedThreadPool();

  private ConcurrentHashMap<String, List<MessageHandler>> subscriptions = new ConcurrentHashMap<>();
  private List<MessageHandler> wildcardSubscriptions = new ArrayList<>();

  @Override
  public void send(String destination, Message message) {
    String id = idGenerator.genId().asString();
    message.getHeaders().put(Message.ID, id);
    message.getHeaders().put(Message.DESTINATION, destination);
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
    List<MessageHandler> handlers = subscriptions.getOrDefault(destination, Collections.emptyList());
    sendToHandlers(destination, message, handlers);
    sendToHandlers(destination, message, wildcardSubscriptions);
  }

  private void sendToHandlers(String destination, Message message, List<MessageHandler> handlers) {
    logger.info("sending to channel {} that has {} subscriptions this message {} ", destination, handlers.size(), message);
    for (MessageHandler handler : handlers) {
      executor.execute(() -> transactionTemplate.execute(ts -> {
        try {
          handler.accept(message);
        } catch (Throwable t) {
          logger.error("message handler " + destination, t);
        }
        return null;
      }));
    }
  }

  @Override
  public void subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    if (singleton("*").equals(channels)) {
      logger.info("subscribing {} to wildcard channels", subscriberId);
      wildcardSubscriptions.add(handler);
    } else {
      logger.info("subscribing {} to channels {}", subscriberId, channels);
      for (String channel : channels) {
        List<MessageHandler> handlers = subscriptions.computeIfAbsent(channel, k -> new ArrayList<>());
        handlers.add(handler);
      }
    }
  }
}
