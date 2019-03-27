package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.consumer.common.coordinator.*;
import io.eventuate.tram.redis.common.RedissonClients;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;
import java.util.function.Consumer;

public class RedisCoordinatorFactoryImpl implements CoordinatorFactory {

//  private final RedisTemplate<String, String> redisTemplate;
//  private final RedissonClients redissonClients;
//  private final int partitions;
//  private final long groupMemberTtlInMilliseconds;
//  private final long listenerIntervalInMilliseconds;
//  private final long assignmentTtlInMilliseconds;
//  private final long leadershipTtlInMilliseconds;
//
//  public RedisCoordinatorFactoryImpl(RedisTemplate<String, String> redisTemplate,
//                                     RedissonClients redissonClients,
//                                     int partitions,
//                                     long groupMemberTtlInMilliseconds,
//                                     long listenerIntervalInMilliseconds,
//                                     long assignmentTtlInMilliseconds,
//                                     long leadershipTtlInMilliseconds) {
//    this.redisTemplate = redisTemplate;
//    this.redissonClients = redissonClients;
//    this.partitions = partitions;
//    this.groupMemberTtlInMilliseconds = groupMemberTtlInMilliseconds;
//    this.listenerIntervalInMilliseconds = listenerIntervalInMilliseconds;
//    this.assignmentTtlInMilliseconds = assignmentTtlInMilliseconds;
//    this.leadershipTtlInMilliseconds = leadershipTtlInMilliseconds;
//  }


  private AssignmentManager assignmentManager;
  private AssignmentListenerFactory assignmentListenerFactory;
  private MemberGroupManagerFactory memberGroupManagerFactory;
  private LeaderSelectorFactory leaderSelectorFactory;
  private GroupMemberFactory groupMemberFactory;
  private int partitionCount;

  public RedisCoordinatorFactoryImpl(AssignmentManager assignmentManager,
                                     AssignmentListenerFactory assignmentListenerFactory,
                                     MemberGroupManagerFactory memberGroupManagerFactory,
                                     LeaderSelectorFactory leaderSelectorFactory,
                                     GroupMemberFactory groupMemberFactory,
                                     int partitionCount) {

    this.assignmentManager = assignmentManager;
    this.assignmentListenerFactory = assignmentListenerFactory;
    this.memberGroupManagerFactory = memberGroupManagerFactory;
    this.leaderSelectorFactory = leaderSelectorFactory;
    this.groupMemberFactory = groupMemberFactory;
    this.partitionCount = partitionCount;
  }

  @Override
  public Coordinator makeCoordinator(String subscriberId,
                                     Set<String> channels,
                                     String subscriptionId,
                                     Consumer<Assignment> assignmentUpdatedCallback,
                                     Runnable leaderSelected,
                                     Runnable leaderRemoved) {

    return new Coordinator(subscriptionId,
            subscriberId,
            channels,
            partitionCount,
            groupMemberFactory,
            memberGroupManagerFactory,
            assignmentManager,
            assignmentListenerFactory,
            leaderSelectorFactory,
            assignmentUpdatedCallback,
            leaderSelected,
            leaderRemoved);
  }
}
