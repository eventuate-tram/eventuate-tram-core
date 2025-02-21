package io.eventuate.tram.spring.events.subscriber;

import io.eventuate.tram.events.subscriber.DomainEventDispatcherFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnnotationBasedEventsSubscriberConfiguration {


  @Bean
  public EventuateDomainEventDispatcher eventuateDomainEventDispatcher(DomainEventDispatcherFactory domainEventDispatcherFactory) {
    return new EventuateDomainEventDispatcher(domainEventDispatcherFactory);
  }

  @Bean
  public EventuateDomainEventHandlerBeanPostProcessor eventuateDomainEventHandlerBeanPostProcessor(EventuateDomainEventDispatcher eventuateDomainEventDispatcher) {
    return new EventuateDomainEventHandlerBeanPostProcessor(eventuateDomainEventDispatcher);
  }
}
