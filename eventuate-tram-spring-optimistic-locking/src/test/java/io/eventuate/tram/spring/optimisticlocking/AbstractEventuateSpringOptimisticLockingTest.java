package io.eventuate.tram.spring.optimisticlocking;

import io.eventuate.tram.consumer.common.MessageHandlerDecorator;
import io.eventuate.tram.jdbc.optimistic.locking.common.test.AbstractEventuateOptimisticLockingTest;
import org.springframework.beans.factory.annotation.Autowired;

abstract public class AbstractEventuateSpringOptimisticLockingTest extends AbstractEventuateOptimisticLockingTest {

  @Autowired
  private OptimisticLockingDecorator optimisticLockingDecorator;

  @Override
  public MessageHandlerDecorator getOptimisticLockingDecorator() {
    return optimisticLockingDecorator;
  }
}