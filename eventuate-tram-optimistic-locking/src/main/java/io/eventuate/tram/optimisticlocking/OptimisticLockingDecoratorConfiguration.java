package io.eventuate.tram.optimisticlocking;

import org.springframework.context.annotation.Bean;

public class OptimisticLockingDecoratorConfiguration {
  @Bean
  public OptimisticLockingDecorator customerOptimisticLockingDecorator() {
    return new OptimisticLockingDecorator();
  }
}
