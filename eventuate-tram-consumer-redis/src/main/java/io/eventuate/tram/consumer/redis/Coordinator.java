package io.eventuate.tram.consumer.redis;

import org.redisson.api.RedissonClient;
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
  private RedisTemplate<String, String> redisTemplate;
  private RedissonClient redissonClient;
  private Consumer<Assignment> assignmentUpdatedCallback;
  private int partitionCount;
  private long groupMemberTtlInMilliseconds;
  private long listenerInterval;
  private long leadershipTtlInMilliseconds;

  private RedisGroupMember redisGroupMember;
  private RedisAssignmentListener redisAssignmentListener;
  private RedisLeaderSelector leaderSelector;
  private RedisAssignmentManager redisAssignmentManager;
  private Optional<RedisMemberGroupManager> redisMemberGroupManager = Optional.empty();
  PartitionManager partitionManager;

  private Set<String> previousGroupMembers;

  public Coordinator(RedisTemplate<String, String> redisTemplate,
                     RedissonClient redissonClient,
                     String groupMemberId,
                     String subscriberId,
                     Set<String> channels,
                     Consumer<Assignment> assignmentUpdatedCallback,
                     int partitionCount,
                     long groupMemberTtlInMilliseconds,
                     long listenerIntervalInMilliseconds,
                     long assignmentTtlInMilliseconds,
                     long leadershipTtlInMilliseconds) {


    this.redisTemplate = redisTemplate;
    this.redissonClient = redissonClient;

    redisAssignmentManager = new RedisAssignmentManager(redisTemplate, assignmentTtlInMilliseconds);

    this.groupMemberId = groupMemberId;
    this.subscriberId = subscriberId;
    this.channels = channels;

    this.assignmentUpdatedCallback = assignmentUpdatedCallback;
    this.partitionCount = partitionCount;
    this.groupMemberTtlInMilliseconds = groupMemberTtlInMilliseconds;
    this.listenerInterval = listenerIntervalInMilliseconds;
    this.leadershipTtlInMilliseconds = leadershipTtlInMilliseconds;

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
    leaderSelector = new RedisLeaderSelector(redissonClient,
            subscriberId, leadershipTtlInMilliseconds, this::onLeaderSelected);
  }

  private void onLeaderSelected() {
    partitionManager = new PartitionManager(partitionCount);
    previousGroupMembers = new HashSet<>();

    RedisMemberGroupManager redisMemberGroupManager = new RedisMemberGroupManager(redisTemplate,
            subscriberId,
            listenerInterval,
            this::onGroupMembersUpdated);

    this.redisMemberGroupManager = Optional.of(redisMemberGroupManager);
  }

  private void onGroupMembersUpdated(Set<String> expectedGroupMembers) {
    if (!partitionManager.isInitialized()) {
      initializePartitionManager(expectedGroupMembers);
    } else {
      rebalance(expectedGroupMembers);
    }

    previousGroupMembers = expectedGroupMembers;
  }

  private void initializePartitionManager(Set<String> expectedGroupMembers) {
    Map<String, Assignment> assignments = expectedGroupMembers
            .stream()
            .collect(Collectors.toMap(Function.identity(), this::readAssignment));

    partitionManager
            .initialize(assignments)
            .forEach(this::saveAssignment);
  }

  private void rebalance(Set<String> expectedGroupMembers) {
    Set<String> removedGroupMembers = previousGroupMembers
            .stream()
            .filter(groupMember -> !expectedGroupMembers.contains(groupMember))
            .collect(Collectors.toSet());

    Map<String, Set<String>> addedGroupMembersWithTheirSubscribedChannels = expectedGroupMembers
            .stream()
            .filter(groupMember -> !previousGroupMembers.contains(groupMember))
            .collect(Collectors.toMap(Function.identity(), groupMember -> readAssignment(groupMemberId).getChannels()));

    partitionManager
            .rebalance(addedGroupMembersWithTheirSubscribedChannels, removedGroupMembers)
            .forEach(this::saveAssignment);
  }

  private Assignment readAssignment(String groupMemberId) {
    return redisAssignmentManager.readAssignment(subscriberId, groupMemberId);
  }

  private void saveAssignment(String groupMemberId, Assignment assignment) {
    redisAssignmentManager.createOrUpdateAssignment(subscriberId, groupMemberId, assignment);
  }

  public void close() {
    redisMemberGroupManager.ifPresent(RedisMemberGroupManager::stop);
    redisAssignmentListener.remove();
    redisGroupMember.remove();
    leaderSelector.stop();
  }
}
