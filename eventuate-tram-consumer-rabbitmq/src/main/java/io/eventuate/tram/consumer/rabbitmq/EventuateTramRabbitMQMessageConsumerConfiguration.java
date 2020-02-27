package io.eventuate.tram.consumer.rabbitmq;

import io.eventuate.messaging.rabbitmq.consumer.MessageConsumerRabbitMQConfiguration;
import io.eventuate.messaging.rabbitmq.consumer.MessageConsumerRabbitMQImpl;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.spring.consumer.common.TramConsumerCommonConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MessageConsumerRabbitMQConfiguration.class, TramConsumerCommonConfiguration.class,})
public class EventuateTramRabbitMQMessageConsumerConfiguration {
  @Bean
  public MessageConsumerImplementation messageConsumerImplementation(MessageConsumerRabbitMQImpl messageConsumerRabbitMQ) {
    return new EventuateTramRabbitMQMessageConsumer(messageConsumerRabbitMQ);
  }
}
