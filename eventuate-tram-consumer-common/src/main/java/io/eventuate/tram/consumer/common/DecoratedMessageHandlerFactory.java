package io.eventuate.tram.consumer.common;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class DecoratedMessageHandlerFactory {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final List<MessageHandlerDecorator> decorators;

  public DecoratedMessageHandlerFactory(List<MessageHandlerDecorator> decorators) {
    decorators.sort(Comparator.comparingInt(MessageHandlerDecorator::getOrder));
    this.decorators = decorators;
  }

  public Consumer<SubscriberIdAndMessage> decorate(MessageHandler mh) {
    MessageHandlerDecoratorChainBuilder builder = new MessageHandlerDecoratorChainBuilder();

    for (MessageHandlerDecorator mhd : decorators)
      builder = builder.andThen(mhd);

    MessageHandlerDecoratorChain chain = builder.andFinally((smh) -> {
      String subscriberId = smh.getSubscriberId();
      Message message = smh.getMessage();
      try {
        logger.trace("Invoking handler {} {}", subscriberId, message.getId());
        mh.accept(smh.getMessage());
        logger.trace("handled message {} {}", subscriberId, message.getId());
      } catch (Exception e) {
        logger.error("Got exception {} {}", subscriberId, message.getId());
        logger.error("Got exception ", e);
        throw e;
      }
    });
    return chain::invokeNext;
  }
}
