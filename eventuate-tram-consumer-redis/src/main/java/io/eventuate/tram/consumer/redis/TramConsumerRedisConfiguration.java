package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.redis.common.CommonRedisConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Import({TramConsumerCommonConfiguration.class, CommonRedisConfiguration.class})
public class TramConsumerRedisConfiguration {
  @Bean
  public MessageConsumer messageConsumer(RedisTemplate<String, String> redisTemplate) {
    return new MessageConsumerRedisImpl(redisTemplate);
  }
}
