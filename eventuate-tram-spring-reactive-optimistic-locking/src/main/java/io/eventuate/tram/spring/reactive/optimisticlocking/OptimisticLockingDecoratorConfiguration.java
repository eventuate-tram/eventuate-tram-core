package io.eventuate.tram.spring.reactive.optimisticlocking;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OptimisticLockingDecoratorConfiguration {

    @Bean
    public OptimisticLockingDecorator reactiveOptimisticLockingDecorator() {
        return new OptimisticLockingDecorator();
    }
}
