package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public interface ReactiveDuplicateMessageDetector {
  Mono<Boolean> isDuplicate(SubscriberIdAndMessage subscriberIdAndMessage);
  Supplier<Mono<Void>> doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Supplier<Mono<Void>> processingFlow);
}
