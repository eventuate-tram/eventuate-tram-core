package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.consumer.common.coordinator.Assignment;
import io.eventuate.tram.redis.common.RedissonClients;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;
import java.util.function.Consumer;

public class RedisCoordinatorFactoryImpl implements RedisCoordinatorFactory {

  private final RedisTemplate<String, String> redisTemplate;
  private final RedissonClients redissonClients;
  private final int partitions;
  private final long groupMemberTtlInMilliseconds;
  private final long listenerIntervalInMilliseconds;
  private final long assignmentTtlInMilliseconds;
  private final long leadershipTtlInMilliseconds;

  public RedisCoordinatorFactoryImpl(RedisTemplate<String, String> redisTemplate,
                                     RedissonClients redissonClients,
                                     int partitions,
                                     long groupMemberTtlInMilliseconds,
                                     long listenerIntervalInMilliseconds,
                                     long assignmentTtlInMilliseconds,
                                     long leadershipTtlInMilliseconds) {
    this.redisTemplate = redisTemplate;
    this.redissonClients = redissonClients;
    this.partitions = partitions;
    this.groupMemberTtlInMilliseconds = groupMemberTtlInMilliseconds;
    this.listenerIntervalInMilliseconds = listenerIntervalInMilliseconds;
    this.assignmentTtlInMilliseconds = assignmentTtlInMilliseconds;
    this.leadershipTtlInMilliseconds = leadershipTtlInMilliseconds;
  }

  @Override
  public Coordinator makeCoordinator(String subscriberId, Set<String> channels, String subscriptionId,
                                     Consumer<Assignment> assignmentUpdatedCallback) {
    return new Coordinator(redisTemplate,
            redissonClients,
            subscriptionId,
            subscriberId,
            channels,
            assignmentUpdatedCallback,
            partitions,
            groupMemberTtlInMilliseconds,
            listenerIntervalInMilliseconds,
            assignmentTtlInMilliseconds,
            leadershipTtlInMilliseconds,
            createGroupMember(subscriberId, subscriptionId),
            (groupMembersUpdatedCallback) ->
                    new RedisMemberGroupManager(redisTemplate, subscriberId, listenerIntervalInMilliseconds, groupMembersUpdatedCallback),
            createRedisAssignmentManager(),
            () -> new RedisAssignmentListener(redisTemplate, subscriberId, subscriptionId, listenerIntervalInMilliseconds, assignmentUpdatedCallback),
            (leaderSelectedCallback, leaderRemovedCallback) ->
                    new RedisLeaderSelector(redissonClients, subscriberId, leadershipTtlInMilliseconds, leaderSelectedCallback));
  }

  private RedisGroupMember createGroupMember(String subscriberId, String groupMemberId) {
    return new RedisGroupMember(redisTemplate, subscriberId, groupMemberId, groupMemberTtlInMilliseconds);
  }

  private RedisAssignmentManager createRedisAssignmentManager() {
    return new RedisAssignmentManager(redisTemplate, assignmentTtlInMilliseconds);
  }
}
