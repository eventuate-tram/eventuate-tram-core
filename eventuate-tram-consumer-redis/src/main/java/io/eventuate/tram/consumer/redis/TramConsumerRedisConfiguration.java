package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.consumer.common.coordinator.*;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.redis.common.CommonRedisConfiguration;
import io.eventuate.tram.redis.common.RedisConfigurationProperties;
import io.eventuate.tram.redis.common.RedissonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Import({TramConsumerCommonConfiguration.class, CommonRedisConfiguration.class})
public class TramConsumerRedisConfiguration {

  @Bean
  public MessageConsumer messageConsumer(RedisTemplate<String, String> redisTemplate,
                                         CoordinatorFactory coordinatorFactory,
                                         RedisConfigurationProperties redisConfigurationProperties) {

    return new MessageConsumerRedisImpl(redisTemplate,
            coordinatorFactory,
            redisConfigurationProperties.getTimeInMillisecondsToSleepWhenKeyDoesNotExist(),
            redisConfigurationProperties.getBlockStreamTimeInMilliseconds());
  }

  @Bean
  public CoordinatorFactory redisCoordinatorFactory(AssignmentManager assignmentManager,
                                                    AssignmentListenerFactory assignmentListenerFactory,
                                                    MemberGroupManagerFactory memberGroupManagerFactory,
                                                    LeaderSelectorFactory leaderSelectorFactory,
                                                    GroupMemberFactory groupMemberFactory,
                                                    RedisConfigurationProperties redisConfigurationProperties) {
    return new CoordinatorFactoryImpl(assignmentManager,
            assignmentListenerFactory,
            memberGroupManagerFactory,
            leaderSelectorFactory,
            groupMemberFactory,
            redisConfigurationProperties.getPartitions());
  }

  @Bean
  public GroupMemberFactory groupMemberFactory(RedisTemplate<String, String> redisTemplate,
                                               RedisConfigurationProperties redisConfigurationProperties) {
    return (groupId, memberId) ->
            new RedisGroupMember(redisTemplate,
                    groupId,
                    memberId,
                    redisConfigurationProperties.getGroupMemberTtlInMilliseconds());
  }

  @Bean
  public LeaderSelectorFactory leaderSelectorFactory(RedissonClients redissonClients,
                                                     RedisConfigurationProperties redisConfigurationProperties) {
    return (groupId, memberId, leaderSelectedCallback, leaderRemovedCallback) ->
            new RedisLeaderSelector(redissonClients,
                    groupId,
                    memberId,
                    redisConfigurationProperties.getLeadershipTtlInMilliseconds(),
                    leaderSelectedCallback,
                    leaderRemovedCallback);
  }

  @Bean
  public MemberGroupManagerFactory memberGroupManagerFactory(RedisTemplate<String, String> redisTemplate,
                                                             RedisConfigurationProperties redisConfigurationProperties) {
    return (groupId, memberId, groupMembersUpdatedCallback) ->
            new RedisMemberGroupManager(redisTemplate,
                    groupId,
                    memberId,
                    redisConfigurationProperties.getListenerIntervalInMilliseconds(),
                    groupMembersUpdatedCallback);
  }

  @Bean
  public AssignmentListenerFactory assignmentListenerFactory(RedisTemplate<String, String> redisTemplate,
                                                             RedisConfigurationProperties redisConfigurationProperties) {
    return (groupId, memberId, assignmentUpdatedCallback) ->
            new RedisAssignmentListener(redisTemplate,
                    groupId,
                    memberId,
                    redisConfigurationProperties.getListenerIntervalInMilliseconds(),
                    assignmentUpdatedCallback);
  }

  @Bean
  public AssignmentManager assignmentManager(RedisTemplate<String, String> redisTemplate,
                                             RedisConfigurationProperties redisConfigurationProperties) {
    return new RedisAssignmentManager(redisTemplate, redisConfigurationProperties.getAssignmentTtlInMilliseconds());
  }
}
