package io.eventuate.tram.spring.messaging.producer.jdbc.reactive;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.reactive.jdbc.EventuateCommonReactiveJdbcOperations;
import io.eventuate.common.spring.jdbc.reactive.EventuateCommonReactiveDatabaseConfiguration;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducerImplementation;
import io.eventuate.tram.reactive.messaging.producer.jdbc.ReactiveMessageProducerJdbcImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateCommonReactiveDatabaseConfiguration.class)
public class ReactiveTramMessageProducerJdbcConfiguration {

  @Bean
  @ConditionalOnMissingBean(ReactiveMessageProducerImplementation.class)
  public ReactiveMessageProducerImplementation reactiveMessageProducerJdbc(EventuateCommonReactiveJdbcOperations eventuateCommonReactiveJdbcOperations,
                                                                     IdGenerator idGenerator,
                                                                     EventuateSchema eventuateSchema) {
    return new ReactiveMessageProducerJdbcImpl(eventuateCommonReactiveJdbcOperations,
            idGenerator,
            eventuateSchema);
  }

  @Autowired(required=false)
  private MessageInterceptor[] messageInterceptors = new MessageInterceptor[0];

  @Bean
  public ReactiveMessageProducer reactiveMessageProducer(ChannelMapping channelMapping,
                                                         ReactiveMessageProducerJdbcImpl reactiveMessageProducerJdbc) {
    return new ReactiveMessageProducer(messageInterceptors, channelMapping, reactiveMessageProducerJdbc);
  }
}
