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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class InMemoryMessaging implements MessageProducer, MessageConsumer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private IdGenerator idGenerator;

  @Autowired
  private TransactionTemplate transactionTemplate;

  private Executor executor = Executors.newCachedThreadPool();

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
    List<MessageHandler> handlers = subscriptions.getOrDefault(destination, Collections.emptyList());
    logger.info("sending to channel {} that has {} subscriptions this message {} ", destination, handlers.size(), message);
    for (MessageHandler handler : handlers) {
      executor.execute(() -> transactionTemplate.execute(ts -> {
        handler.accept(message);
        return null;
      }));
    }
  }

  private Map<String, List<MessageHandler>> subscriptions = new HashMap<>();

  @Override
  public void subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    logger.info("subscribing {} to channels {}", subscriberId, channels);
    for (String channel : channels) {
      List<MessageHandler> handlers = subscriptions.get(channel);
      if (handlers == null) {
        handlers = new ArrayList<>();
        subscriptions.put(channel, handlers);
      }
      handlers.add(handler);
    }
  }
}
