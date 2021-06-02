package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import reactor.core.publisher.Mono;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class DecoratedReactiveMessageHandlerFactory {

  private List<ReactiveMessageHandlerDecorator> decorators;

  public DecoratedReactiveMessageHandlerFactory(List<ReactiveMessageHandlerDecorator> decorators) {
    this.decorators = decorators;
    decorators.sort(Comparator.comparingInt(ReactiveMessageHandlerDecorator::getOrder));
  }

  public Function<SubscriberIdAndMessage, Mono<Void>> decorate(ReactiveMessageHandler reactiveMessageHandler) {
    return subscriberIdAndMessage -> {

      Mono<Void> processingFlow = Mono.defer(() -> reactiveMessageHandler.apply(subscriberIdAndMessage.getMessage()));

      ReactiveMessageHandlerDecoratorChain decoratorChain = new ReactiveMessageHandlerDecoratorChain(decorators);

      return decoratorChain.next(subscriberIdAndMessage, processingFlow);
    };
  }
}
