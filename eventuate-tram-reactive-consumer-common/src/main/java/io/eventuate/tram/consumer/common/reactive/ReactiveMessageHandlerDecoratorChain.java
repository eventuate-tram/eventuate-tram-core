package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ReactiveMessageHandlerDecoratorChain {
  private ConcurrentLinkedQueue<ReactiveMessageHandlerDecorator> decorators = new ConcurrentLinkedQueue<>();
  private ReactiveMessageHandler reactiveMessageHandler;

  public ReactiveMessageHandlerDecoratorChain(List<ReactiveMessageHandlerDecorator> decorators,
                                              ReactiveMessageHandler reactiveMessageHandler) {
    this.decorators.addAll(decorators);

    this.reactiveMessageHandler = reactiveMessageHandler;
  }

  public Publisher<?> next(SubscriberIdAndMessage subscriberIdAndMessage) {

    ReactiveMessageHandlerDecorator decorator = decorators.poll();

    if (decorator != null) {
      return decorator.accept(subscriberIdAndMessage, this);
    }

    return Mono.defer(() -> Mono.from(reactiveMessageHandler.apply(subscriberIdAndMessage.getMessage())));
  }
}
