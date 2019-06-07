package io.eventuate.tram.consumer.redis;

import io.eventuate.messaging.redis.consumer.MessageConsumerRedisImpl;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventuateTramRedisMessageConsumerConfiguration {
  @Bean
  public MessageConsumerImplementation messageConsumerImplementation(MessageConsumerRedisImpl messageConsumerRedis) {
    return new EventuateTramRedisMessageConsumer(messageConsumerRedis);
  }
}
