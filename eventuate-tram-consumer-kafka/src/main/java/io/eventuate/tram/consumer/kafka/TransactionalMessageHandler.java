package io.eventuate.tram.consumer.kafka;

import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class TransactionalMessageHandler {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private String subscriberId;
  private MessageHandler handler;
  private final MessageInterceptor[] messageInterceptors;
  private final DuplicateMessageDetector duplicateMessageDetector;
  private final TransactionTemplate transactionTemplate;


  public TransactionalMessageHandler(String subscriberId, MessageHandler handler, MessageInterceptor[] messageInterceptors, DuplicateMessageDetector duplicateMessageDetector, TransactionTemplate transactionTemplate) {
    this.subscriberId = subscriberId;
    this.handler = handler;
    this.messageInterceptors = messageInterceptors;
    this.duplicateMessageDetector = duplicateMessageDetector;
    this.transactionTemplate = transactionTemplate;
  }

  public void handle(Message message, BiConsumer<Void, Throwable> callback) {
    preReceive(message);
    try {
      transactionTemplate.execute(ts -> {
        if (duplicateMessageDetector.isDuplicate(subscriberId, message.getId())) {
          logger.trace("Duplicate message {} {}", subscriberId, message.getId());
          callback.accept(null, null);
          return null;
        }
        try {
          logger.trace("Invoking handler {} {}", subscriberId, message.getId());
          preHandle(subscriberId, message);
          handler.accept(message);
          postHandle(subscriberId, message, null);
        } catch (Throwable t) {
          postHandle(subscriberId, message, t);
          logger.trace("Got exception {} {}", subscriberId, message.getId());
          logger.trace("Got exception ", t);
          ts.setRollbackOnly();
          callback.accept(null, t);
           throw t instanceof RuntimeException ? (RuntimeException)t : new RuntimeException(t);
        }
        logger.trace("handled message {} {}", subscriberId, message.getId());
        callback.accept(null, null);
        return null;
      });
    } finally {
      postReceive(message);
    }
  }

  private void preReceive(Message message) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.preReceive(message));
  }


  private void preHandle(String subscriberId, Message message) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.preHandle(subscriberId, message));
  }

  private void postHandle(String subscriberId, Message message, Throwable t) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.postHandle(subscriberId, message, t));
  }

  private void postReceive(Message message) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.postReceive(message));
  }


}
