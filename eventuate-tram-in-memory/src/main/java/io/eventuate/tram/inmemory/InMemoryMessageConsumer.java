package io.eventuate.tram.inmemory;


import io.eventuate.tram.consumer.common.DecoratedMessageHandlerFactory;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.common.SubscriberIdAndMessage;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static java.util.Collections.singleton;

public class InMemoryMessageConsumer implements MessageConsumerImplementation {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private DecoratedMessageHandlerFactory decoratedMessageHandlerFactory;

  private Executor executor = Executors.newCachedThreadPool();

  private ConcurrentHashMap<String, List<MessageHandlerWithSubscriberId>> subscriptions = new ConcurrentHashMap<>();
  private List<MessageHandlerWithSubscriberId> wildcardSubscriptions = new ArrayList<>();

  public InMemoryMessageConsumer() {
  }

  public void dispatchMessage(Message message) {
    String destination = message.getRequiredHeader(Message.DESTINATION);
    List<MessageHandlerWithSubscriberId> handlers = subscriptions.getOrDefault(destination, Collections.emptyList());
    logger.info("sending to channel {} that has {} subscriptions this message {} ", destination, handlers.size(), message);
    dispatchMessageToHandlers(destination, message, handlers);
    logger.info("sending to wildcard channel {} that has {} subscriptions this message {} ", destination, wildcardSubscriptions.size(), message);
    dispatchMessageToHandlers(destination, message, wildcardSubscriptions);
  }

  private void dispatchMessageToHandlers(String destination, Message message, List<MessageHandlerWithSubscriberId> handlers) {
    for (MessageHandlerWithSubscriberId handler : handlers) {
      executor.execute(() -> handler.getMessageHandler().accept(new SubscriberIdAndMessage(handler.getSubscriber(), message)));
    }
  }

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    Consumer<SubscriberIdAndMessage> mh = decoratedMessageHandlerFactory.decorate(handler);

    MessageHandlerWithSubscriberId mhwsid = new MessageHandlerWithSubscriberId(subscriberId, mh);
    if (singleton("*").equals(channels)) {
      logger.info("subscribing {} to wildcard channels", subscriberId);
      wildcardSubscriptions.add(mhwsid);
    } else {
      logger.info("subscribing {} to channels {}", subscriberId, channels);
      for (String channel : channels) {
        List<MessageHandlerWithSubscriberId> handlers = subscriptions.computeIfAbsent(channel, k -> new ArrayList<>());
        handlers.add(mhwsid);
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
