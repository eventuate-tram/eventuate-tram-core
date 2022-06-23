package io.eventuate.tram.spring.reactive.optimisticlocking;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(OptimisticLockingDecoratorConfiguration.class)
public class OptimisticLockingDecoratorAutoConfiguration {
}
