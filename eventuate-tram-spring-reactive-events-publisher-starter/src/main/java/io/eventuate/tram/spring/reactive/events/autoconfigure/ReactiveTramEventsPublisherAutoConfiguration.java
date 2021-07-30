package io.eventuate.tram.spring.reactive.events.autoconfigure;

import io.eventuate.tram.spring.events.publisher.ReactiveTramEventsPublisherConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ReactiveTramEventsPublisherConfiguration.class)
public class ReactiveTramEventsPublisherAutoConfiguration {
}
