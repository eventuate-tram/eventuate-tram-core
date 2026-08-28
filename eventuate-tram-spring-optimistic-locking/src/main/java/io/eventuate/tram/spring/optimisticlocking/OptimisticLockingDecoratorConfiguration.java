package io.eventuate.tram.spring.optimisticlocking;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;

@Configuration
@EnableResilientMethods
public class OptimisticLockingDecoratorConfiguration {
  @Bean
  public OptimisticLockingDecorator optimisticLockingDecorator() {
    return new OptimisticLockingDecorator();
  }
}
