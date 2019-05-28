package io.eventuate.jdbcrabbitmq;

import io.eventuate.messaging.rabbitmq.consumer.MessageConsumerRabbitMQImpl;
import io.eventuate.messaging.rabbitmq.consumer.TramConsumerRabbitMQConfiguration;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.consumer.wrappers.EventuateRabbitMQMessageConsumerWrapper;
import io.eventuate.tram.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramConsumerRabbitMQConfiguration.class,
        TramMessageProducerJdbcConfiguration.class,
        TramConsumerCommonConfiguration.class})
public class TramJdbcRabbitMQConfiguration {
  @Bean
  public MessageConsumerImplementation messageConsumerImplementation(MessageConsumerRabbitMQImpl messageConsumerRabbitMQ) {
    return new EventuateRabbitMQMessageConsumerWrapper(messageConsumerRabbitMQ);
  }
}
