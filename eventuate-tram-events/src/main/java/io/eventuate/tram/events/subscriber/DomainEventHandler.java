package io.eventuate.tram.events.subscriber;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DomainEventHandler {
}
