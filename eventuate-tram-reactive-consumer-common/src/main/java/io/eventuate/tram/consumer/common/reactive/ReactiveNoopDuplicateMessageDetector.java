package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public class ReactiveNoopDuplicateMessageDetector implements ReactiveDuplicateMessageDetector {
  @Override
  public Mono<Boolean> isDuplicate(SubscriberIdAndMessage subscriberIdAndMessage) {
    return Mono.just(false);
  }

  @Override
  public Supplier<Mono<Void>> doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Supplier<Mono<Void>> processingFlow) {
    return processingFlow;
  }
}
