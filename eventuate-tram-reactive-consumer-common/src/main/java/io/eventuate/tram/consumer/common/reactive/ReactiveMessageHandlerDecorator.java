package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import reactor.core.publisher.Mono;

public interface ReactiveMessageHandlerDecorator {
  Mono<Void> accept(SubscriberIdAndMessage subscriberIdAndMessage, Mono<Void> processingFlow);

  int getOrder();
}
