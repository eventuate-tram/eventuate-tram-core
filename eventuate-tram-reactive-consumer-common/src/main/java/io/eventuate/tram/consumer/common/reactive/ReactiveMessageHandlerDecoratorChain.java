package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ReactiveMessageHandlerDecoratorChain {
  private ConcurrentLinkedQueue<ReactiveMessageHandlerDecorator> decorators = new ConcurrentLinkedQueue<>();
  private ReactiveMessageHandler reactiveMessageHandler;

  public ReactiveMessageHandlerDecoratorChain(List<ReactiveMessageHandlerDecorator> decorators,
                                              ReactiveMessageHandler reactiveMessageHandler) {
    this.decorators.addAll(decorators);

    this.reactiveMessageHandler = reactiveMessageHandler;
  }

  public Mono<Void> next(SubscriberIdAndMessage subscriberIdAndMessage) {
    return Optional
            .ofNullable(decorators.poll())
            .map(d -> d.accept(subscriberIdAndMessage, this))
            .orElse(Mono.defer(() -> reactiveMessageHandler.apply(subscriberIdAndMessage.getMessage())));
  }
}
