package io.eventuate.tram.optimisticlocking;

import io.eventuate.tram.consumer.common.MessageHandlerDecorator;
import io.eventuate.tram.consumer.common.MessageHandlerDecoratorChain;
import io.eventuate.tram.consumer.common.SubscriberIdAndMessage;
import org.springframework.core.Ordered;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class OptimisticLockingDecorator implements MessageHandlerDecorator, Ordered {

  @Override
  @Retryable(value = OptimisticLockingFailureException.class,
          maxAttempts = 10,
          backoff = @Backoff(delay = 100))
  public void accept(SubscriberIdAndMessage subscriberIdAndMessage, MessageHandlerDecoratorChain messageHandlerDecoratorChain) {
    messageHandlerDecoratorChain.invokeNext(subscriberIdAndMessage);
  }

  @Override
  public int getOrder() {
    return 150;
  }
}
