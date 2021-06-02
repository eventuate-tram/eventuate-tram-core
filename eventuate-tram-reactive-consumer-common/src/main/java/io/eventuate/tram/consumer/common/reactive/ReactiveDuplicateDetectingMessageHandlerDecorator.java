package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import io.eventuate.tram.messaging.consumer.BuiltInMessageHandlerDecoratorOrder;
import reactor.core.publisher.Mono;

public class ReactiveDuplicateDetectingMessageHandlerDecorator implements ReactiveMessageHandlerDecorator {

  private ReactiveDuplicateMessageDetector duplicateMessageDetector;

  public ReactiveDuplicateDetectingMessageHandlerDecorator(ReactiveDuplicateMessageDetector duplicateMessageDetector) {
    this.duplicateMessageDetector = duplicateMessageDetector;
  }

  @Override
  public Mono<Void> accept(SubscriberIdAndMessage subscriberIdAndMessage,
                           ReactiveMessageHandlerDecoratorChain decoratorChain) {

    return duplicateMessageDetector
            .isDuplicate(subscriberIdAndMessage)
            .flatMap(dup -> {
              if (dup) return Mono.empty();
              else return duplicateMessageDetector.doWithMessage(subscriberIdAndMessage, decoratorChain.next(subscriberIdAndMessage));
            });
  }

  @Override
  public int getOrder() {
    return BuiltInMessageHandlerDecoratorOrder.DUPLICATE_DETECTING_MESSAGE_HANDLER_DECORATOR;
  }
}
