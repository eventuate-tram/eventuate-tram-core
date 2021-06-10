package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public interface ReactiveMessageHandlerDecorator {
  Publisher<?> accept(SubscriberIdAndMessage subscriberIdAndMessage, ReactiveMessageHandlerDecoratorChain decoratorChain);

  int getOrder();
}
