package io.eventuate.tram.spring.messaging.producer.jdbc.reactive;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.spring.jdbc.reactive.EventuateCommonReactiveDatabaseConfiguration;
import io.eventuate.common.spring.jdbc.reactive.EventuateCommonReactiveSpringJdbcOperations;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateCommonReactiveDatabaseConfiguration.class)
public class ReactiveTramMessageProducerJdbcConfiguration {

  @Bean
  @ConditionalOnMissingBean(MessageProducerImplementation.class)
  public SpringReactiveMessageProducerJdbc reactiveMessageProducerJdbc(EventuateCommonReactiveSpringJdbcOperations eventuateCommonReactiveSpringJdbcOperations,
                                                                       IdGenerator idGenerator,
                                                                       EventuateSchema eventuateSchema) {
    return new SpringReactiveMessageProducerJdbc(eventuateCommonReactiveSpringJdbcOperations,
            idGenerator,
            eventuateSchema);
  }

  @Autowired(required=false)
  private MessageInterceptor[] messageInterceptors = new MessageInterceptor[0];

  @Bean
  public SpringReactiveMessageProducer reactiveMessageProducer(ChannelMapping channelMapping,
                                                               SpringReactiveMessageProducerJdbc springReactiveMessageProducerJdbc) {
    return new SpringReactiveMessageProducer(messageInterceptors, channelMapping, springReactiveMessageProducerJdbc);
  }
}
