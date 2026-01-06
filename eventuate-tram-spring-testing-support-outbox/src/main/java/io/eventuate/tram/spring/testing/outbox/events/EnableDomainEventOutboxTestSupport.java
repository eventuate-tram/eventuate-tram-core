package io.eventuate.tram.spring.testing.outbox.events;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(DomainEventOutboxTestSupportConfiguration.class)
public @interface EnableDomainEventOutboxTestSupport {
}
