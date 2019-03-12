package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.redis.common.CommonRedisConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Import({TramConsumerCommonConfiguration.class, CommonRedisConfiguration.class})
public class TramConsumerRedisConfiguration {
  @Bean
  public MessageConsumer messageConsumer(RedisTemplate<String, String> redisTemplate,
                                         @Value("${eventuatelocal.zookeeper.connection.string}") String zkUrl,
                                         @Value("${redis.partitions}") int redisPartitions,
                                         @Value("${redis.group.member.ttl.in.milliseconds:#{10000}}")
                                                   long redisGroupMemberTtlInMilliseconds,
                                         @Value("${redis.listener.interval.in.milliseconds:#{1000}}")
                                                   long redisListenerIntervalInMilliseconds,
                                         @Value("${redis.assignment.ttl.in.milliseconds:#{36000000}}")
                                                   long redisAssignmentTtlInMilliseconds) {

    return new MessageConsumerRedisImpl(zkUrl,
            redisTemplate,
            redisPartitions,
            redisGroupMemberTtlInMilliseconds,
            redisListenerIntervalInMilliseconds,
            redisAssignmentTtlInMilliseconds);
  }
}
