package io.eventuate.tram.consumer.rabbitmq;

import io.eventuate.messaging.rabbitmq.consumer.MessageConsumerRabbitMQImpl;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventuateTramRabbitMQMessageConsumerConfiguration {
  @Bean
  public MessageConsumerImplementation messageConsumerImplementation(MessageConsumerRabbitMQImpl messageConsumerRabbitMQ) {
    return new EventuateTramRabbitMQMessageConsumer(messageConsumerRabbitMQ);
  }
}
