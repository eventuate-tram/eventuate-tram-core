package io.eventuate.tram.spring.events.autoconfigure;

import io.eventuate.tram.spring.events.subscriber.TramEventSubscriberConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(TramEventSubscriberConfiguration.class)
@Import(TramEventSubscriberConfiguration.class)
public class TramEventsSubscriberAutoConfiguration {
}
