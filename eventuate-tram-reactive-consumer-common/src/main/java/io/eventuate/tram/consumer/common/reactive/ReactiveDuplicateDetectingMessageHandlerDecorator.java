package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import io.eventuate.tram.messaging.consumer.BuiltInMessageHandlerDecoratorOrder;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class ReactiveDuplicateDetectingMessageHandlerDecorator implements ReactiveMessageHandlerDecorator {

  private final ReactiveDuplicateMessageDetector duplicateMessageDetector;

  public ReactiveDuplicateDetectingMessageHandlerDecorator(ReactiveDuplicateMessageDetector duplicateMessageDetector) {
    this.duplicateMessageDetector = duplicateMessageDetector;
  }

  @Override
  public Publisher<?> accept(SubscriberIdAndMessage subscriberIdAndMessage,
                             ReactiveMessageHandlerDecoratorChain decoratorChain) {

      return duplicateMessageDetector.doWithMessage(subscriberIdAndMessage,
              Mono.defer(() -> Mono.from(decoratorChain.next(subscriberIdAndMessage))));
  }

  @Override
  public int getOrder() {
    return BuiltInMessageHandlerDecoratorOrder.DUPLICATE_DETECTING_MESSAGE_HANDLER_DECORATOR;
  }
}
