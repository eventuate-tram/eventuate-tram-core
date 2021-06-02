package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ReactiveMessageHandlerDecoratorChain {
  ConcurrentLinkedQueue<ReactiveMessageHandlerDecorator> decorators = new ConcurrentLinkedQueue<>();

  public ReactiveMessageHandlerDecoratorChain(List<ReactiveMessageHandlerDecorator> decorators) {
    this.decorators.addAll(decorators);
  }

  public Mono<Void> next(SubscriberIdAndMessage subscriberIdAndMessage, Mono<Void> processingFlow) {
    return Optional
            .ofNullable(decorators.poll())
            .map(d -> d.accept(subscriberIdAndMessage, processingFlow, this))
            .orElse(processingFlow);
  }
}
