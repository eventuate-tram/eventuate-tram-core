package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import reactor.core.publisher.Mono;

public interface ReactiveDuplicateMessageDetector {
  Mono<Boolean> isDuplicate(SubscriberIdAndMessage subscriberIdAndMessage);
  Mono<Void> doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Mono<Void> processingFlow);
}
