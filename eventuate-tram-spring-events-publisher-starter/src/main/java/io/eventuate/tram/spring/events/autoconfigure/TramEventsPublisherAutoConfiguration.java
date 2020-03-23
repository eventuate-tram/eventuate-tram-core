package io.eventuate.tram.spring.events.autoconfigure;

import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.spring.events.publisher.TramEventsPublisherConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(TramEventsPublisherConfiguration.class)
@Import(TramEventsPublisherConfiguration.class)
public class TramEventsPublisherAutoConfiguration {
}
