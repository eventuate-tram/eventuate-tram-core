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
  public Mono<Void> accept(SubscriberIdAndMessage subscriberIdAndMessage, Mono<Void> processingFlow) {
    return duplicateMessageDetector.doWithMessage(subscriberIdAndMessage, processingFlow);
  }

  @Override
  public int getOrder() {
    return BuiltInMessageHandlerDecoratorOrder.DUPLICATE_DETECTING_MESSAGE_HANDLER_DECORATOR;
  }
}
