package io.eventuate.tram.micronaut.spring.jdbc.optimistic.locking;

import io.eventuate.tram.consumer.common.MessageHandlerDecorator;
import io.eventuate.tram.consumer.common.MessageHandlerDecoratorChain;
import io.eventuate.tram.consumer.common.SubscriberIdAndMessage;
import io.micronaut.retry.annotation.Retryable;
import io.micronaut.spring.tx.annotation.Transactional;
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException;

@Transactional
public class OptimisticLockingDecorator implements MessageHandlerDecorator {

  @Override
  @Retryable(value = HibernateOptimisticLockingFailureException.class, attempts = "10", delay = "100ms")
  public void accept(SubscriberIdAndMessage subscriberIdAndMessage, MessageHandlerDecoratorChain messageHandlerDecoratorChain) {
    messageHandlerDecoratorChain.invokeNext(subscriberIdAndMessage);
  }

  @Override
  public int getOrder() {
    return 150;
  }
}
