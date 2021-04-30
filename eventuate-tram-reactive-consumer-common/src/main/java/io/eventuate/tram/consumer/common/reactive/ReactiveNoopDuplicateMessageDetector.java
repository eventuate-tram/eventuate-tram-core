package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import reactor.core.publisher.Mono;

public class ReactiveNoopDuplicateMessageDetector implements ReactiveDuplicateMessageDetector {
  @Override
  public Mono<Boolean> isDuplicate(Mono<SubscriberIdAndMessage> subscriberIdAndMessage) {
    return Mono.just(false);
  }

  @Override
  public Mono<SubscriberIdAndMessage> doWithMessage(Mono<SubscriberIdAndMessage> subscriberIdAndMessage) {
    return subscriberIdAndMessage;
  }
}
