package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public interface ReactiveDuplicateMessageDetector {
  Mono<Boolean> isDuplicate(SubscriberIdAndMessage subscriberIdAndMessage);
  Publisher<?> doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Publisher<?> processingFlow);
}
