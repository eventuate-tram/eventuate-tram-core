package io.eventuate.tram.micronaut.data.jdbc.optimistic.locking;

import io.eventuate.tram.consumer.common.MessageHandlerDecorator;
import io.eventuate.tram.jdbc.optimistic.locking.common.test.AbstractEventuateOptimisticLockingTest;

import jakarta.inject.Inject;

abstract public class AbstractEventuateMicronautOptimisticLockingTest extends AbstractEventuateOptimisticLockingTest {

  @Inject
  private OptimisticLockingDecorator optimisticLockingDecorator;

  @Override
  public MessageHandlerDecorator getOptimisticLockingDecorator() {
    return optimisticLockingDecorator;
  }
}