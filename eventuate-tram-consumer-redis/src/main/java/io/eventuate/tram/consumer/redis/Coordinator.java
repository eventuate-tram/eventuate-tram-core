package io.eventuate.tram.consumer.redis;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.CancelLeadershipException;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Coordinator {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private final String groupMemberId;
  private final String subscriberId;
  private Set<String> channels;

  private Consumer<Assignment> assignmentUpdatedCallback;
  private int partitionCount;
  private long groupMemberTtlInMilliseconds;
  private long listenerInterval;

  private String leaderPath;

  private volatile boolean running = true;

  private CuratorFramework curatorFramework;
  private RedisGroupMember redisGroupMember;
  private RedisAssignmentListener redisAssignmentListener;
  private LeaderSelector leaderSelector;

  private Set<String> previousGroupMembers;

  private RedisTemplate<String, String> redisTemplate;
  private RedisAssignmentManager redisAssignmentManager;

  public Coordinator(RedisTemplate<String, String> redisTemplate,
                     String groupMemberId,
                     String zkUrl,
                     String subscriberId,
                     Set<String> channels,
                     Consumer<Assignment> assignmentUpdatedCallback,
                     int partitionCount,
                     long groupMemberTtlInMilliseconds,
                     long listenerIntervalInMilliseconds,
                     long assignmentTtlInMilliseconds) {


    this.redisTemplate = redisTemplate;
    redisAssignmentManager = new RedisAssignmentManager(redisTemplate, assignmentTtlInMilliseconds);

    this.groupMemberId = groupMemberId;
    this.subscriberId = subscriberId;
    this.channels = channels;

    this.assignmentUpdatedCallback = assignmentUpdatedCallback;
    this.partitionCount = partitionCount;
    this.groupMemberTtlInMilliseconds = groupMemberTtlInMilliseconds;
    this.listenerInterval = listenerIntervalInMilliseconds;

    leaderPath = String.format("/eventuate-tram/redis/consumer-leaders/%s", subscriberId);

    curatorFramework = CuratorFrameworkFactory.newClient(zkUrl,
            new ExponentialBackoffRetry(1000, 5));

    curatorFramework.start();

    createInitialAssignments();
    createGroupMember();
    createAssignmentListeners();
    createLeaderSelector();
  }

  private void createInitialAssignments() {
    try {
      Map<String, Set<Integer>> partitionAssignmentsByChannel = new HashMap<>();
      channels.forEach(channel -> partitionAssignmentsByChannel.put(channel, new HashSet<>()));
      Assignment assignment = new Assignment(channels, partitionAssignmentsByChannel);

      saveAssignment(groupMemberId, assignment);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private void createGroupMember() {
    redisGroupMember = new RedisGroupMember(redisTemplate, subscriberId, groupMemberId, groupMemberTtlInMilliseconds);
  }

  private void createAssignmentListeners() {
    redisAssignmentListener = new RedisAssignmentListener(redisTemplate,
            subscriberId,
            groupMemberId,
            listenerInterval,
            assignmentUpdatedCallback);
  }

  private void createLeaderSelector() {
    leaderSelector = new LeaderSelector(curatorFramework, leaderPath, new LeaderSelectorListener() {
      @Override
      public void takeLeadership(CuratorFramework client) {

        PartitionManager partitionManager = new PartitionManager(partitionCount);
        previousGroupMembers = new HashSet<>();

        RedisMemberGroupManager redisMemberGroupManager = new RedisMemberGroupManager(redisTemplate,
                subscriberId,
                listenerInterval,
                currentGroupMembers -> {
          if (!partitionManager.isInitialized()) {
            Map<String, Assignment> assignments = currentGroupMembers
                    .stream()
                    .collect(Collectors.toMap(Function.identity(), groupMemberId -> readAssignment(groupMemberId)));

            partitionManager
                    .initialize(assignments)
                    .forEach((memberId, assignment) -> saveAssignment(memberId, assignment));
          } else {

            Set<String> removedGroupMembers = previousGroupMembers
                    .stream()
                    .filter(groupMember -> !currentGroupMembers.contains(groupMember))
                    .collect(Collectors.toSet());

            Map<String, Set<String>> addedGroupMembersWithTheirSubscribedChannels = currentGroupMembers
                    .stream()
                    .filter(groupMember -> !previousGroupMembers.contains(groupMember))
                    .collect(Collectors.toMap(Function.identity(), groupMember -> readAssignment(groupMemberId).getChannels()));

            partitionManager
                    .rebalance(addedGroupMembersWithTheirSubscribedChannels, removedGroupMembers)
                    .forEach((memberId, assignment) -> saveAssignment(memberId, assignment));
          }

          previousGroupMembers = currentGroupMembers;
        });

        try {
          while (running) {
            try {
              Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
              break;
            }
          }
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
        } finally {
          redisMemberGroupManager.stop();
        }
      }

      @Override
      public void stateChanged(CuratorFramework client, ConnectionState newState) {
        if (newState == ConnectionState.SUSPENDED || newState == ConnectionState.LOST) {
          throw new CancelLeadershipException();
        }
      }
    });

    leaderSelector.autoRequeue();

    leaderSelector.start();
  }

  private Assignment readAssignment(String groupMemberId) {
    return redisAssignmentManager.readAssignment(subscriberId, groupMemberId);
  }

  private void saveAssignment(String groupMemberId, Assignment assignment) {
    redisAssignmentManager.createOrUpdateAssignment(subscriberId, groupMemberId, assignment);
  }

  public void close() {
    running = false;
    leaderSelector.close();
    redisAssignmentListener.remove();
    redisGroupMember.remove();
    curatorFramework.close();
  }
}
