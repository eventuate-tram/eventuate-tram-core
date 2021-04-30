package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import reactor.core.publisher.Mono;

public interface ReactiveDuplicateMessageDetector {
  Mono<Boolean> isDuplicate(Mono<SubscriberIdAndMessage> subscriberIdAndMessage);
  Mono<SubscriberIdAndMessage> doWithMessage(Mono<SubscriberIdAndMessage> subscriberIdAndMessage);
}
