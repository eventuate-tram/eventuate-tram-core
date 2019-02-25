package io.eventuate.tram.cdc.mysql.connector.configuration;

import io.eventuate.local.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.tram.data.producer.redis.EventuateRedisProducer;
import io.eventuate.tram.redis.common.CommonRedisConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Import(CommonRedisConfiguration.class)
public class RedisMessageTableChangesToDestinationsConfiguration {
  @Bean
  @Profile("Redis")
  public PublishingFilter redisDuplicatePublishingDetector() {
    return (fileOffset, topic) -> true;
  }

  @Bean
  @Profile("Redis")
  public DataProducerFactory redisDataProducerFactory(RedisTemplate<String, String> redisTemplate) {
    return () -> new EventuateRedisProducer(redisTemplate);
  }
}
