package io.eventuate.tram.reactive.integrationtests;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageHandlerDecorator;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import io.eventuate.tram.reactive.events.subscriber.ReactiveDomainEventDispatcher;
import io.eventuate.tram.reactive.events.subscriber.ReactiveDomainEventDispatcherFactory;
import io.eventuate.tram.spring.events.publisher.ReactiveDomainEventPublisher;
import io.eventuate.tram.spring.events.publisher.ReactiveTramEventsPublisherConfiguration;
import io.eventuate.tram.spring.messaging.producer.jdbc.reactive.ReactiveTramMessageProducerJdbcConfiguration;
import io.eventuate.tram.spring.reactive.consumer.common.ReactiveTramConsumerCommonConfiguration;
import io.eventuate.tram.spring.reactive.consumer.kafka.EventuateTramReactiveKafkaMessageConsumerConfiguration;
import io.eventuate.tram.spring.reactive.events.subscriber.ReactiveTramEventSubscriberConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReactiveTramEventIntegrationTest.Config.class)
public class ReactiveTramEventIntegrationTest {

  @Import({ReactiveTramMessageProducerJdbcConfiguration.class,
          ReactiveTramEventsPublisherConfiguration.class,
          ReactiveTramEventSubscriberConfiguration.class,
          ReactiveTramConsumerCommonConfiguration.class,
          EventuateTramReactiveKafkaMessageConsumerConfiguration.class})
  @EnableAutoConfiguration
  public static class Config {

    @Bean
    public ReactiveDomainEventDispatcher reactiveDomainEventDispatcher(ReactiveDomainEventDispatcherFactory reactiveDomainEventDispatcherFactory,
                                                                       ReactiveTramTestEventConsumer reactiveTramTestEventConsumer) {

      return reactiveDomainEventDispatcherFactory.make(IdSupplier.get(), reactiveTramTestEventConsumer.domainEventHandlers());
    }

    @Bean
    public ReactiveTramTestEventConsumer reactiveTramTestEventConsumer() {
      return new ReactiveTramTestEventConsumer(UUID.randomUUID().toString());
    }

    @Bean
    public ReactiveMessageHandlerDecorator filter() {
      return new ReactiveMessageHandlerDecorator() {

        @Override
        public Mono<SubscriberIdAndMessage> preHandler(Mono<SubscriberIdAndMessage> subscriberIdAndMessage) {
          return  subscriberIdAndMessage.flatMap(siam -> {
            if (siam.getMessage().getPayload().contains("ignored")) {
              return Mono.empty();
            } else {
              return Mono.just(siam);
            }
          });
        }

        @Override
        public int getOrder() {
          return 0;
        }
      };
    }
  }

  @Autowired
  private ReactiveDomainEventPublisher domainEventPublisher;

  @Autowired
  private ReactiveTramTestEventConsumer tramTestEventConsumer;

  private String aggregateId;
  private String payload;

  @Before
  public void init() {
    aggregateId = IdSupplier.get();
    payload = IdSupplier.get();
  }

  @Test
  public void shouldSendAndReceiveEvent() throws InterruptedException {
    domainEventPublisher
            .publish(tramTestEventConsumer.getAggregateType(), aggregateId, Collections.singletonList(new TestEvent(payload)))
            .collectList()
            .block();

    TestEvent event = tramTestEventConsumer.getQueue().poll(10, TimeUnit.SECONDS);

    Assert.assertEquals(payload, event.getPayload());
  }

  @Test
  public void shouldNotHandleFilteredEvents() throws InterruptedException {
    domainEventPublisher
            .publish(tramTestEventConsumer.getAggregateType(), aggregateId, Collections.singletonList(new TestEvent(payload + "ignored")))
            .collectList()
            .block();

    TestEvent event = tramTestEventConsumer.getQueue().poll(5, TimeUnit.SECONDS);

    Assert.assertNull(event);
  }
}
