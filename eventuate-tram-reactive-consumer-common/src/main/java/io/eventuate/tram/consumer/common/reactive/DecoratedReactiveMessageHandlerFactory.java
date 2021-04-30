package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class DecoratedReactiveMessageHandlerFactory {

  private List<ReactiveMessageHandlerDecorator> decorators;
  private List<ReactiveMessageHandlerDecorator> decoratorsWithReversedOrder;

  public DecoratedReactiveMessageHandlerFactory(List<ReactiveMessageHandlerDecorator> decorators) {
    this.decorators = decorators;
    decorators.sort(Comparator.comparingInt(ReactiveMessageHandlerDecorator::getOrder));

    decoratorsWithReversedOrder = new ArrayList<>(decorators);
    Collections.reverse(decoratorsWithReversedOrder);
  }

  public Function<SubscriberIdAndMessage, Mono<SubscriberIdAndMessage>> decorate(ReactiveMessageHandler reactiveMessageHandler) {
    return subscriberIdAndMessage -> {

      Mono<SubscriberIdAndMessage> message = Mono.just(subscriberIdAndMessage);

      for (ReactiveMessageHandlerDecorator decorator : decorators) {
        message = decorator.preHandler(message);
      }

      message = executeMessageHandler(message, reactiveMessageHandler);

      for (ReactiveMessageHandlerDecorator decorator : decoratorsWithReversedOrder) {
        message = decorator.postHandler(message);
      }

      return message;
    };
  }

  private Mono<SubscriberIdAndMessage> executeMessageHandler(Mono<SubscriberIdAndMessage> message,
                                                             ReactiveMessageHandler reactiveMessageHandler) {
    return message
            .flatMap(msg ->
                    reactiveMessageHandler
                            .apply(msg.getMessage())
                            .map(m -> new SubscriberIdAndMessage(msg.getSubscriberId(), m)));
  }
}
