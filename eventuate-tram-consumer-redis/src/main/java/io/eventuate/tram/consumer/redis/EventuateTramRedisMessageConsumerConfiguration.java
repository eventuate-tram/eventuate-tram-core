package io.eventuate.tram.consumer.redis;

import io.eventuate.messaging.redis.spring.consumer.MessageConsumerRedisConfiguration;
import io.eventuate.messaging.redis.spring.consumer.MessageConsumerRedisImpl;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.spring.consumer.common.TramConsumerCommonConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MessageConsumerRedisConfiguration.class, TramConsumerCommonConfiguration.class,})
public class EventuateTramRedisMessageConsumerConfiguration {
  @Bean
  public MessageConsumerImplementation messageConsumerImplementation(MessageConsumerRedisImpl messageConsumerRedis) {
    return new EventuateTramRedisMessageConsumer(messageConsumerRedis);
  }
}
