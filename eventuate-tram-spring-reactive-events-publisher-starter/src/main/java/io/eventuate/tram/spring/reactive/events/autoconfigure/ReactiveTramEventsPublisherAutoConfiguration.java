package io.eventuate.tram.spring.reactive.events.autoconfigure;

import io.eventuate.tram.spring.events.publisher.ReactiveTramEventsPublisherConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(ReactiveTramEventsPublisherConfiguration.class)
public class ReactiveTramEventsPublisherAutoConfiguration {
}
