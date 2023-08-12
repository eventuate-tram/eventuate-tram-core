package io.eventuate.tram.spring.events.autoconfigure;

import io.eventuate.tram.spring.events.subscriber.TramEventSubscriberConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnClass(TramEventSubscriberConfiguration.class)
@Import(TramEventSubscriberConfiguration.class)
public class TramEventsSubscriberAutoConfiguration {
}
