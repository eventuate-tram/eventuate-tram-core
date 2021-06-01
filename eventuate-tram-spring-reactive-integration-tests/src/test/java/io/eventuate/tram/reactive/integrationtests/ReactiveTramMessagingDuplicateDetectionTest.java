package io.eventuate.tram.reactive.integrationtests;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumerImplementation;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.spring.messaging.producer.jdbc.reactive.ReactiveTramMessageProducerJdbcConfiguration;
import io.eventuate.tram.spring.reactive.consumer.common.ReactiveTramConsumerCommonConfiguration;
import io.eventuate.tram.spring.reactive.consumer.kafka.EventuateTramReactiveKafkaMessageConsumerConfiguration;
import io.eventuate.util.test.async.Eventually;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReactiveTramMessagingDuplicateDetectionTest.Config.class, properties = "spring.main.allow-bean-definition-overriding=true")
public class ReactiveTramMessagingDuplicateDetectionTest {

  @Import({ReactiveTramMessageProducerJdbcConfiguration.class,
          EventuateTramReactiveKafkaMessageConsumerConfiguration.class,
          ReactiveTramConsumerCommonConfiguration.class})
  @EnableAutoConfiguration
  public static class Config {

    @Bean
    @Primary
    public TransactionalOperator transactionalOperator() {
      TransactionalOperator transactionalOperator = Mockito.mock(TransactionalOperator.class);

      Mockito.when(transactionalOperator.transactional((Mono<? extends Object>) Mockito.any())).thenReturn(Mono.empty());

      return transactionalOperator;
    }

    @Bean
    @Primary
    public ReactiveMessageConsumerImplementation reactiveMessageConsumerImplementation() {
      String id = UUID.randomUUID().toString();

      return new ReactiveMessageConsumerImplementation() {
        @Override
        public MessageSubscription subscribe(String subscriberId, Set<String> channels, ReactiveMessageHandler handler) {
          handler.apply(MessageBuilder.withPayload(id).withHeader("ID", id).build()).block();
          handler.apply(MessageBuilder.withPayload(id).withHeader("ID", id).build()).block();

          return () -> {};
        }

        @Override
        public String getId() {
          return UUID.randomUUID().toString();
        }

        @Override
        public void close() {}
      };
    }
  }

  @Autowired
  protected ReactiveMessageConsumer messageConsumer;

  @Autowired
  protected TransactionalOperator transactionalOperator;

  @Test
  public void shouldInvokeTransactionalForDuplicate() {
    messageConsumer.subscribe(IdSupplier.get(), Collections.singleton(IdSupplier.get()), message -> Mono.empty());

    Eventually.eventually(() ->
            Mockito.verify(transactionalOperator, Mockito.times(2)).transactional((Mono<?>) Mockito.any()));
  }
}
