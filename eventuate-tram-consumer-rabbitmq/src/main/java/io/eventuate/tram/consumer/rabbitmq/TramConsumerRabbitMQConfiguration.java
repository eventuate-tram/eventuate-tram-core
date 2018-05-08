package io.eventuate.tram.consumer.rabbitmq;

import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TramConsumerCommonConfiguration.class)
public class TramConsumerRabbitMQConfiguration {
  @Bean
  public MessageConsumer messageConsumer(@Value("${rabbitmq.url}") String rabbitMQURL) {
    return new MessageConsumerRabbitMQImpl(rabbitMQURL);
  }
}
