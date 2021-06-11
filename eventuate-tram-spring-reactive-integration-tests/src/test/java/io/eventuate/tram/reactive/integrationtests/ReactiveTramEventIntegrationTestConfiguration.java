package io.eventuate.tram.reactive.integrationtests;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageHandlerDecorator;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageHandlerDecoratorChain;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import io.eventuate.tram.reactive.events.subscriber.ReactiveDomainEventDispatcher;
import io.eventuate.tram.reactive.events.subscriber.ReactiveDomainEventDispatcherFactory;
import io.eventuate.tram.spring.events.publisher.ReactiveDomainEventPublisher;
import io.eventuate.tram.spring.events.publisher.ReactiveTramEventsPublisherConfiguration;
import io.eventuate.tram.spring.messaging.producer.jdbc.reactive.ReactiveTramMessageProducerJdbcConfiguration;
import io.eventuate.tram.spring.reactive.consumer.common.ReactiveTramConsumerCommonConfiguration;
import io.eventuate.tram.spring.reactive.consumer.kafka.EventuateTramReactiveKafkaMessageConsumerConfiguration;
import io.eventuate.tram.spring.reactive.events.subscriber.ReactiveTramEventSubscriberConfiguration;
import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Import({ReactiveTramMessageProducerJdbcConfiguration.class,
        ReactiveTramEventsPublisherConfiguration.class,
        ReactiveTramEventSubscriberConfiguration.class,
        ReactiveTramConsumerCommonConfiguration.class,
        EventuateTramReactiveKafkaMessageConsumerConfiguration.class})
@EnableAutoConfiguration
public class ReactiveTramEventIntegrationTestConfiguration {

    @Bean
    public ReactiveDomainEventDispatcher reactiveDomainEventDispatcher(ReactiveDomainEventDispatcherFactory reactiveDomainEventDispatcherFactory,
                                                                       ReactiveTramTestEventConsumer reactiveTramTestEventConsumer) {

      return reactiveDomainEventDispatcherFactory.make(IdSupplier.get(), reactiveTramTestEventConsumer.domainEventHandlers());
    }

  @Bean
  public ReactiveDomainEventDispatcher additionalReactiveDomainEventDispatcher(ReactiveDomainEventDispatcherFactory reactiveDomainEventDispatcherFactory,
                                                                               ReactiveTramAdditionalTestEventConsumer additionalTestEventConsumer) {

    return reactiveDomainEventDispatcherFactory.make(IdSupplier.get(), additionalTestEventConsumer.domainEventHandlers());
  }

  @Bean
  public ReactiveTramTestEventConsumer reactiveTramTestEventConsumer(ReactiveDomainEventPublisher domainEventPublisher) {
    return new ReactiveTramTestEventConsumer(IdSupplier.get(), domainEventPublisher);
  }

  @Bean
  public ReactiveTramAdditionalTestEventConsumer reactiveTramAdditionalTestEventConsumer(ReactiveDomainEventPublisher domainEventPublisher) {
    return new ReactiveTramAdditionalTestEventConsumer(IdSupplier.get());
  }

    @Bean
    public ReactiveMessageHandlerDecorator decoratorThatFilterEventsThatShouldBeIgnored() {
      return new ReactiveMessageHandlerDecorator() {

        @Override
        public Publisher<?> accept(SubscriberIdAndMessage subscriberIdAndMessage, ReactiveMessageHandlerDecoratorChain decoratorChain) {

          if (subscriberIdAndMessage.getMessage().getPayload().contains("ignored")) {
            return Mono.empty();
          }
          else return decoratorChain.next(subscriberIdAndMessage);
        }

        @Override
        public int getOrder() {
          return 0;
        }
      };
    }
}
