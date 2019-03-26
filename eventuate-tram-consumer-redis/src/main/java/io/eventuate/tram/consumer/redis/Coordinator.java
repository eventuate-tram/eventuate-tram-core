package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.consumer.common.coordinator.*;
import io.eventuate.tram.redis.common.RedissonClients;
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
  private RedissonClients redissonClients;
  private Consumer<Assignment> assignmentUpdatedCallback;
  private int partitionCount;
  private long groupMemberTtlInMilliseconds;
  private long listenerInterval;
  private long leadershipTtlInMilliseconds;

  private GroupMember groupMember;
  private MemberGroupManagerFactory memberGroupManagerFactory;
  private AssignmentListener assignmentListener;
  private CommonLeaderSelector leaderSelector;
  private AssignmentManager assignmentManager;
  private Optional<MemberGroupManager> memberGroupManager = Optional.empty();
  private PartitionManager partitionManager;

  private Set<String> previousGroupMembers;

  public Coordinator(RedisTemplate<String, String> redisTemplate,
                     RedissonClients redissonClients,
                     String groupMemberId,
                     String subscriberId,
                     Set<String> channels,
                     Consumer<Assignment> assignmentUpdatedCallback,
                     int partitionCount,
                     long groupMemberTtlInMilliseconds,
                     long listenerIntervalInMilliseconds,
                     long assignmentTtlInMilliseconds,
                     long leadershipTtlInMilliseconds,
                     GroupMember groupMember,
                     MemberGroupManagerFactory memberGroupManagerFactory,
                     AssignmentManager assignmentManager,
                     AssignmentListenerFactory assignmentListenerFactory,
                     LeaderSelectorFactory leaderSelectorFactory) {


    this.redisTemplate = redisTemplate;
    this.redissonClients = redissonClients;

    this.assignmentManager = assignmentManager;

    this.groupMemberId = groupMemberId;
    this.subscriberId = subscriberId;
    this.channels = channels;

    this.assignmentUpdatedCallback = assignmentUpdatedCallback;
    this.partitionCount = partitionCount;
    this.groupMemberTtlInMilliseconds = groupMemberTtlInMilliseconds;
    this.listenerInterval = listenerIntervalInMilliseconds;
    this.leadershipTtlInMilliseconds = leadershipTtlInMilliseconds;

    createInitialAssignments();

    this.memberGroupManagerFactory = memberGroupManagerFactory;
    this.groupMember = groupMember;

    assignmentListener = assignmentListenerFactory.create();
    leaderSelector = leaderSelectorFactory.create(this::onLeaderSelected, () -> {});
  }

  private void createInitialAssignments() {
    try {
      Map<String, Set<Integer>> partitionAssignmentsByChannel = new HashMap<>();
      channels.forEach(channel -> partitionAssignmentsByChannel.put(channel, new HashSet<>()));
      Assignment assignment = new Assignment(channels, partitionAssignmentsByChannel);

      assignmentManager.initializeAssignment(subscriberId, groupMemberId, assignment);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private void createLeaderSelector() {
    leaderSelector = new RedisLeaderSelector(redissonClients,
            subscriberId, leadershipTtlInMilliseconds, this::onLeaderSelected);
  }

  private void onLeaderSelected() {
    partitionManager = new PartitionManager(partitionCount);
    previousGroupMembers = new HashSet<>();
    memberGroupManager = Optional.of(memberGroupManagerFactory.create(this::onGroupMembersUpdated));
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
    return assignmentManager.readAssignment(subscriberId, groupMemberId);
  }

  private void saveAssignment(String groupMemberId, Assignment assignment) {
    assignmentManager.saveAssignment(subscriberId, groupMemberId, assignment);
  }

  public void close() {
    memberGroupManager.ifPresent(MemberGroupManager::stop);
    assignmentListener.remove();
    groupMember.remove();
    leaderSelector.stop();
  }
}
