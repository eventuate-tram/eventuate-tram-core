package io.eventuate.tram.spring.events.autoconfigure;

import io.eventuate.tram.spring.events.publisher.TramEventsPublisherConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnClass(TramEventsPublisherConfiguration.class)
@Import(TramEventsPublisherConfiguration.class)
public class TramEventsPublisherAutoConfiguration {
}
