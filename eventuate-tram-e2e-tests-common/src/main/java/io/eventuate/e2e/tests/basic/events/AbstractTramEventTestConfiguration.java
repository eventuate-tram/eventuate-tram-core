package io.eventuate.e2e.tests.basic.events;

import io.eventuate.tram.events.publisher.TramEventsPublisherConfiguration;
import io.eventuate.tram.events.subscriber.DomainEventDispatcher;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TramEventsPublisherConfiguration.class)
public class AbstractTramEventTestConfiguration {

  @Bean
  public AbstractTramEventTestConfig abstractTramEventTestConfig() {
    return new AbstractTramEventTestConfig();
  }

  @Bean
  public DomainEventDispatcher domainEventDispatcher(AbstractTramEventTestConfig config,
                                                     TramEventTestEventConsumer target,
                                                     MessageConsumer messageConsumer) {
    return new DomainEventDispatcher("eventDispatcherId" + config.getUniqueId(),
            target.domainEventHandlers(),
            messageConsumer);
  }

  @Bean
  public TramEventTestEventConsumer tramEventTestTarget(AbstractTramEventTestConfig config) {
    return new TramEventTestEventConsumer(config.getAggregateType());
  }


}
