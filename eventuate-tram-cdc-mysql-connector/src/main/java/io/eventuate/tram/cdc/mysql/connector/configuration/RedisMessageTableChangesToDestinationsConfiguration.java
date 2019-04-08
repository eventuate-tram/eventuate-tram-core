package io.eventuate.tram.cdc.mysql.connector.configuration;

import io.eventuate.coordination.leadership.LeaderSelectorFactory;
import io.eventuate.local.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.tram.redis.common.RedisLeaderSelector;
import io.eventuate.tram.data.producer.redis.EventuateRedisProducer;
import io.eventuate.tram.redis.common.CommonRedisConfiguration;
import io.eventuate.tram.redis.common.RedisConfigurationProperties;
import io.eventuate.tram.redis.common.RedissonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Import(CommonRedisConfiguration.class)
@Profile("Redis")
public class RedisMessageTableChangesToDestinationsConfiguration {
  @Bean
  public PublishingFilter redisDuplicatePublishingDetector() {
    return (fileOffset, topic) -> true;
  }

  @Bean
  public DataProducerFactory redisDataProducerFactory(RedisTemplate<String, String> redisTemplate,
                                                      RedisConfigurationProperties redisConfigurationProperties) {
    return () -> new EventuateRedisProducer(redisTemplate, redisConfigurationProperties.getPartitions());
  }

  @Bean
  public LeaderSelectorFactory connectorLeaderSelectorFactory(RedissonClients redissonClients) {
    return (lockId, leaderId, leaderSelectedCallback, leaderRemovedCallback) ->
            new RedisLeaderSelector(redissonClients, lockId, leaderId, 10000, leaderSelectedCallback, leaderRemovedCallback);
  }
}
