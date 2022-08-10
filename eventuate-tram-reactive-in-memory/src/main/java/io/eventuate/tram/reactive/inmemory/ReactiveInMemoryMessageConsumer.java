package io.eventuate.tram.reactive.inmemory;


import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumerImplementation;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageHandler;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.util.Collections.singleton;

public class ReactiveInMemoryMessageConsumer implements ReactiveMessageConsumerImplementation {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final Executor executor = Executors.newCachedThreadPool();

  private final ConcurrentHashMap<String, List<ReactiveMessageHandler>> subscriptions = new ConcurrentHashMap<>();
  private final List<ReactiveMessageHandler> wildcardSubscriptions = new ArrayList<>();

  public ReactiveInMemoryMessageConsumer() {
  }

  public void dispatchMessage(Message message) {
    String destination = message.getRequiredHeader(Message.DESTINATION);
    List<ReactiveMessageHandler> handlers = subscriptions.getOrDefault(destination, Collections.emptyList());
    logger.info("sending to channel {} that has {} subscriptions this message {} ", destination, handlers.size(), message);
    dispatchMessageToHandlers(destination, message, handlers);
    logger.info("sending to wildcard channel {} that has {} subscriptions this message {} ", destination, wildcardSubscriptions.size(), message);
    dispatchMessageToHandlers(destination, message, wildcardSubscriptions);
  }

  private void dispatchMessageToHandlers(String destination, Message message, List<ReactiveMessageHandler> handlers) {
    for (ReactiveMessageHandler handler : handlers) {
      executor.execute(() -> Mono.from(handler.apply(message)).block());
    }
  }

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, ReactiveMessageHandler handler) {
    if (singleton("*").equals(channels)) {
      logger.info("subscribing {} to wildcard channels", subscriberId);
      wildcardSubscriptions.add(handler);
    } else {
      logger.info("subscribing {} to channels {}", subscriberId, channels);
      for (String channel : channels) {
        List<ReactiveMessageHandler> handlers = subscriptions.computeIfAbsent(channel, k -> new ArrayList<>());
        handlers.add(handler);
      }
    }
    return () -> {
      logger.info("Closing in-memory consumer");
      wildcardSubscriptions.remove(handler);
      for (String channel : channels) {
        subscriptions.get(channel).remove(handler);
      }
      logger.info("Closed in-memory consumer");
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
