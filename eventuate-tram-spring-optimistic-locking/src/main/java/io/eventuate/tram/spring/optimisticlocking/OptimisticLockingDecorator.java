package io.eventuate.tram.spring.optimisticlocking;

import io.eventuate.tram.consumer.common.MessageHandlerDecorator;
import io.eventuate.tram.consumer.common.MessageHandlerDecoratorChain;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import org.springframework.core.Ordered;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class OptimisticLockingDecorator implements MessageHandlerDecorator, Ordered {

  @Override
  @Retryable(includes = {OptimisticLockingFailureException.class},
          maxRetries = 9,
          delay = 100)
  public void accept(SubscriberIdAndMessage subscriberIdAndMessage, MessageHandlerDecoratorChain messageHandlerDecoratorChain) {
    messageHandlerDecoratorChain.invokeNext(subscriberIdAndMessage);
  }

  @Override
  public int getOrder() {
    return 150;
  }
}
