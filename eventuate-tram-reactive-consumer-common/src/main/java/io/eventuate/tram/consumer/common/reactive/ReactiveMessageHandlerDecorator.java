package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import reactor.core.publisher.Mono;

public interface ReactiveMessageHandlerDecorator {
  default Mono<SubscriberIdAndMessage> preHandler(Mono<SubscriberIdAndMessage> subscriberIdAndMessage) {
    return subscriberIdAndMessage;
  }

  default Mono<SubscriberIdAndMessage> postHandler(Mono<SubscriberIdAndMessage> subscriberIdAndMessage) {
    return subscriberIdAndMessage;
  }

  int getOrder();
}
