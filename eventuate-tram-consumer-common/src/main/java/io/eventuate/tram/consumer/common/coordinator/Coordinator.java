package io.eventuate.tram.consumer.common.coordinator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Coordinator {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private final String subscriptionId;
  private final String subscriberId;
  private Set<String> channels;
  private int partitionCount;
  private GroupMember groupMember;
  private MemberGroupManagerFactory memberGroupManagerFactory;
  private AssignmentListener assignmentListener;
  private CommonLeaderSelector leaderSelector;
  private AssignmentManager assignmentManager;
  private MemberGroupManager memberGroupManager;
  private PartitionManager partitionManager;
  private Set<String> previousGroupMembers;
  private Runnable leaderSelected;
  private Runnable leaderRemoved;

  public Coordinator(String subscriptionId,
                     String subscriberId,
                     Set<String> channels,
                     int partitionCount,
                     GroupMemberFactory groupMemberFactory,
                     MemberGroupManagerFactory memberGroupManagerFactory,
                     AssignmentManager assignmentManager,
                     AssignmentListenerFactory assignmentListenerFactory,
                     LeaderSelectorFactory leaderSelectorFactory,
                     Consumer<Assignment> assignmentUpdatedCallback,
                     Runnable leaderSelected,
                     Runnable leaderRemoved) {

    this.leaderSelected = leaderSelected;
    this.leaderRemoved = leaderRemoved;
    this.subscriptionId = subscriptionId;
    this.subscriberId = subscriberId;
    this.channels = channels;
    this.partitionCount = partitionCount;
    this.assignmentManager = assignmentManager;
    this.groupMember = groupMemberFactory.create(subscriberId, subscriptionId);
    this.memberGroupManagerFactory = memberGroupManagerFactory;
    createInitialAssignments();
    assignmentListener = assignmentListenerFactory.create(subscriberId, subscriptionId, assignmentUpdatedCallback);
    leaderSelector = leaderSelectorFactory.create(subscriberId, this::onLeaderSelected, this::onLeaderRemoved);
  }

  private void createInitialAssignments() {
    try {
      Map<String, Set<Integer>> partitionAssignmentsByChannel = new HashMap<>();
      channels.forEach(channel -> partitionAssignmentsByChannel.put(channel, new HashSet<>()));
      Assignment assignment = new Assignment(channels, partitionAssignmentsByChannel);

      assignmentManager.initializeAssignment(subscriberId, subscriptionId, assignment);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private void onLeaderSelected() {
    leaderSelected.run();
    partitionManager = new PartitionManager(partitionCount);
    previousGroupMembers = new HashSet<>();
    memberGroupManager = memberGroupManagerFactory.create(subscriberId, Coordinator.this::onGroupMembersUpdated);
  }

  private void onLeaderRemoved() {
    leaderRemoved.run();
    memberGroupManager.stop();
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
            .collect(Collectors.toMap(Function.identity(), groupMember -> readAssignment(subscriptionId).getChannels()));

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
    assignmentListener.remove();
    groupMember.remove();
    leaderSelector.stop();
  }
}
