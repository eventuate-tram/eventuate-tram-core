package io.eventuate.tram.consumer.common;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.OrderComparator;

import java.util.List;
import java.util.function.Consumer;

public class DecoratedMessageHandlerFactory {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private List<MessageHandlerDecorator> decorators;

  public DecoratedMessageHandlerFactory(List<MessageHandlerDecorator> decorators) {
    decorators.sort(OrderComparator.INSTANCE);
    this.decorators = decorators;
  }

  public Consumer<SubscriberIdAndMessage> decorate(MessageHandler mh) {
    MessageHandlerDecoratorChainBuilder builder = MessageHandlerDecoratorChainBuilder.startingWith(decorators.get(0));

    for (MessageHandlerDecorator mhd : decorators.subList(1, decorators.size()))
      builder = builder.andThen(mhd);

    MessageHandlerDecoratorChain chain = builder.andFinally((smh) -> {
      String subscriberId = smh.getSubscriberId();
      Message message = smh.getMessage();
      try {
        logger.trace("Invoking handler {} {}", subscriberId, message.getId());
        mh.accept(smh.getMessage());
        logger.trace("handled message {} {}", subscriberId, message.getId());
      } catch (Exception e) {
        logger.trace("Got exception {} {}", subscriberId, message.getId());
        logger.trace("Got exception ", e);
        throw e;
      }
    });
    return chain::invokeNext;
  }
}
