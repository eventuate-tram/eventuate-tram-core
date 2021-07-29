package io.eventuate.tram.spring.reactive.events.autoconfigure;

import io.eventuate.tram.spring.reactive.events.subscriber.ReactiveTramEventSubscriberConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(ReactiveTramEventSubscriberConfiguration.class)
@Import(ReactiveTramEventSubscriberConfiguration.class)
public class ReactiveTramEventsSubscriberAutoConfiguration {
}
