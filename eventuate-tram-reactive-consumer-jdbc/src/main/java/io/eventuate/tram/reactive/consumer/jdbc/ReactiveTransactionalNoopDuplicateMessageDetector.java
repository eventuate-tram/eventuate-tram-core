package io.eventuate.tram.reactive.consumer.jdbc;

import io.eventuate.tram.consumer.common.reactive.ReactiveDuplicateMessageDetector;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import org.reactivestreams.Publisher;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

public class ReactiveTransactionalNoopDuplicateMessageDetector implements ReactiveDuplicateMessageDetector {

  private TransactionalOperator transactionalOperator;

  public ReactiveTransactionalNoopDuplicateMessageDetector(TransactionalOperator transactionalOperator) {
    this.transactionalOperator = transactionalOperator;
  }

  @Override
  public Mono<Boolean> isDuplicate(SubscriberIdAndMessage subscriberIdAndMessage) {
    return Mono.just(false);
  }

  @Override
  public Publisher<?> doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Publisher<?> processingFlow) {
    return Mono.defer(() -> Mono.from(processingFlow).as(transactionalOperator::transactional));
  }
}
