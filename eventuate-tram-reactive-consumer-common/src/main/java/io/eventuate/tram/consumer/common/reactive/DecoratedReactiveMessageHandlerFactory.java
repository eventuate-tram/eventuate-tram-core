package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import org.reactivestreams.Publisher;
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

  public Function<SubscriberIdAndMessage, Publisher<?>> decorate(ReactiveMessageHandler reactiveMessageHandler) {
    return subscriberIdAndMessage ->
            new ReactiveMessageHandlerDecoratorChain(decorators, reactiveMessageHandler).next(subscriberIdAndMessage);
  }
}
