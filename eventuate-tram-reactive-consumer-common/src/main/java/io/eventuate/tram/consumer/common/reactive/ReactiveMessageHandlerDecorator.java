package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public interface ReactiveMessageHandlerDecorator {
  Supplier<Mono<Void>> accept(SubscriberIdAndMessage subscriberIdAndMessage, Supplier<Mono<Void>> processingFlow);

  int getOrder();
}
