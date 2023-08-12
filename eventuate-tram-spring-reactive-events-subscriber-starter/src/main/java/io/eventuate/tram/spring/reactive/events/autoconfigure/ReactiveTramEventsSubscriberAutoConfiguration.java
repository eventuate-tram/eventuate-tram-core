package io.eventuate.tram.spring.reactive.events.autoconfigure;

import io.eventuate.tram.spring.reactive.events.subscriber.ReactiveTramEventSubscriberConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(ReactiveTramEventSubscriberConfiguration.class)
public class ReactiveTramEventsSubscriberAutoConfiguration {
}
