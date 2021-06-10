package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class ReactiveNoopDuplicateMessageDetector implements ReactiveDuplicateMessageDetector {
  @Override
  public Mono<Boolean> isDuplicate(SubscriberIdAndMessage subscriberIdAndMessage) {
    return Mono.just(false);
  }

  @Override
  public Publisher<?> doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Publisher<?> processingFlow) {
    return processingFlow;
  }
}
