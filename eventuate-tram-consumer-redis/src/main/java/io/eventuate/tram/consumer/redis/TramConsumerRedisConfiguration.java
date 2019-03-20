package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.redis.common.CommonRedisConfiguration;
import io.eventuate.tram.redis.common.RedissonClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Import({TramConsumerCommonConfiguration.class, CommonRedisConfiguration.class})
public class TramConsumerRedisConfiguration {

  @Bean
  public RedisCoordinatorFactory redisCoordinatorFactory(RedisTemplate<String, String> redisTemplate,
                                                         RedissonClients redissonClients,
                                                         @Value("${redis.partitions}") int redisPartitions,
                                                         @Value("${redis.group.member.ttl.in.milliseconds:#{10000}}")
                                                                       long redisGroupMemberTtlInMilliseconds,
                                                         @Value("${redis.listener.interval.in.milliseconds:#{1000}}")
                                                                       long redisListenerIntervalInMilliseconds,
                                                         @Value("${redis.assignment.ttl.in.milliseconds:#{36000000}}")
                                                                       long redisAssignmentTtlInMilliseconds,
                                                         @Value("${redis.leadership.ttl.in.milliseconds:#{10000}}")
                                                                       long redisLeadershipTtlInMilliseconds) {
    return new RedisCoordinatorFactoryImpl(redisTemplate,
            redissonClients,
            redisPartitions,
            redisGroupMemberTtlInMilliseconds,
            redisListenerIntervalInMilliseconds,
            redisAssignmentTtlInMilliseconds,
            redisLeadershipTtlInMilliseconds);
  }
  @Bean
  public MessageConsumer messageConsumer(RedisTemplate<String, String> redisTemplate,
                                         RedisCoordinatorFactory redisCoordinatorFactory,
                                         @Value("${redis.consumer.time.to.sleep.in.milliseconds.when.key.does.not.exist:#{500}}")
                                                   long timeInMillisecondsToSleepWhenKeyDoesNotExist) {

    return new MessageConsumerRedisImpl(redisTemplate,
            redisCoordinatorFactory,
            timeInMillisecondsToSleepWhenKeyDoesNotExist);
  }
}
