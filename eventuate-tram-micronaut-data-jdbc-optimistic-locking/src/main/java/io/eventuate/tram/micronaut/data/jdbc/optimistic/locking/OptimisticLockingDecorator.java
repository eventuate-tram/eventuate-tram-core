package io.eventuate.tram.micronaut.data.jdbc.optimistic.locking;

import io.eventuate.tram.consumer.common.MessageHandlerDecorator;
import io.eventuate.tram.consumer.common.MessageHandlerDecoratorChain;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import io.micronaut.retry.annotation.Retryable;
import io.micronaut.transaction.annotation.TransactionalAdvice;

import javax.persistence.OptimisticLockException;

@TransactionalAdvice
public class OptimisticLockingDecorator implements MessageHandlerDecorator {

  @Override
  @Retryable(value = OptimisticLockException.class, attempts = "10", delay = "100ms")
  @TransactionalAdvice
  public void accept(SubscriberIdAndMessage subscriberIdAndMessage, MessageHandlerDecoratorChain messageHandlerDecoratorChain) {
    messageHandlerDecoratorChain.invokeNext(subscriberIdAndMessage);
  }

  @Override
  public int getOrder() {
    return 150;
  }
}
