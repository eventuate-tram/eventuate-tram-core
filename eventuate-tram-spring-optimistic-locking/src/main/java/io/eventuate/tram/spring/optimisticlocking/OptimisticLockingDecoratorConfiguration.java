package io.eventuate.tram.spring.optimisticlocking;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OptimisticLockingDecoratorConfiguration {
  @Bean
  public OptimisticLockingDecorator optimisticLockingDecorator() {
    return new OptimisticLockingDecorator();
  }
}
