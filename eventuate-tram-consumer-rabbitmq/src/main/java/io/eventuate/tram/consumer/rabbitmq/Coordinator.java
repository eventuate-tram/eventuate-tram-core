package io.eventuate.tram.consumer.rabbitmq;

import io.eventuate.tram.consumer.common.coordinator.*;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Coordinator {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private final String groupMemberId;
  private final String subscriberId;
  private Set<String> channels;

  private Runnable leaderSelectedCallback;
  private Runnable leaderRemovedCallback;
  private int partitionCount;

  private CuratorFramework curatorFramework;
  private GroupMember groupMember;
  private MemberGroupManagerFactory memberGroupManagerFactory;
  private AssignmentManager assignmentManager;
  private AssignmentListener assignmentListener;
  private CommonLeaderSelector leaderSelector;
  private PartitionManager partitionManager = new PartitionManager(partitionCount);
  private MemberGroupManager memberGroupManager;
  private Set<String> previousGroupMembers;

  public Coordinator(String groupMemberId,
                     CuratorFramework curatorFramework,
                     String subscriberId,
                     Set<String> channels,
                     Runnable leaderSelectedCallback,
                     Runnable leaderRemovedCallback,
                     Consumer<Assignment> assignmentUpdatedCallback,
                     int partitionCount,
                     GroupMember groupMember,
                     MemberGroupManagerFactory memberGroupManagerFactory,
                     AssignmentManager assignmentManager,
                     AssignmentListenerFactory assignmentListenerFactory,
                     LeaderSelectorFactory leaderSelectorFactory) {

    this.groupMemberId = groupMemberId;
    this.subscriberId = subscriberId;
    this.channels = channels;

    this.leaderSelectedCallback = leaderSelectedCallback;
    this.leaderRemovedCallback = leaderRemovedCallback;
    this.partitionCount = partitionCount;

    this.curatorFramework = curatorFramework;

    this.groupMember = groupMember;
    this.memberGroupManagerFactory = memberGroupManagerFactory;
    this.assignmentManager = assignmentManager;
    createAssignmentNodes();
    assignmentListener = assignmentListenerFactory.create();
    leaderSelector = leaderSelectorFactory.create(this::onLeaderSelected, this::onLeaderRemoved);
  }

  private void createAssignmentNodes() {
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

  private void onLeaderSelected() {
    partitionManager = new PartitionManager(partitionCount);
    previousGroupMembers = new HashSet<>();
    leaderSelectedCallback.run();
    memberGroupManager = memberGroupManagerFactory.create(Coordinator.this::onGroupMembersUpdated);
  }

  private void onLeaderRemoved() {
    memberGroupManager.stop();
    leaderRemovedCallback.run();
  }

  private void onGroupMembersUpdated(Set<String> currentGroupMembers) {
    if (!partitionManager.isInitialized()) {
      Map<String, Assignment> assignments = currentGroupMembers
              .stream()
              .collect(Collectors.toMap(Function.identity(), this::readAssignment));

      partitionManager
              .initialize(assignments)
              .forEach(this::saveAssignment);
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
              .forEach(this::saveAssignment);
    }

    previousGroupMembers = currentGroupMembers;
  }

  private Assignment readAssignment(String groupMemberId) {
    return assignmentManager.readAssignment(subscriberId, groupMemberId);
  }

  private void saveAssignment(String groupMemberId, Assignment assignment) {
    assignmentManager.saveAssignment(subscriberId, groupMemberId, assignment);
  }

  public void close() {
    leaderSelector.stop();
    assignmentListener.remove();
    groupMember.remove();
    curatorFramework.close();
  }
}
