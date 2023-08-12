package io.eventuate.tram.spring.reactive.optimisticlocking;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(OptimisticLockingDecoratorConfiguration.class)
public class OptimisticLockingDecoratorAutoConfiguration {
}
