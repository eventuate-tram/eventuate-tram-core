package io.eventuate.tram.consumer.common.coordinator;

import io.eventuate.coordination.leadership.EventuateLeaderSelector;
import io.eventuate.coordination.leadership.LeaderSelectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
  private EventuateLeaderSelector leaderSelector;
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
                     String lockId,
                     Runnable leaderSelected,
                     Runnable leaderRemoved) {

    this.leaderSelected = leaderSelected;
    this.leaderRemoved = leaderRemoved;
    this.subscriptionId = subscriptionId;
    this.subscriberId = subscriberId;
    this.channels = channels;
    this.partitionCount = partitionCount;
    this.assignmentManager = assignmentManager;
    this.memberGroupManagerFactory = memberGroupManagerFactory;

    createInitialAssignments();
    groupMember = groupMemberFactory.create(subscriberId, subscriptionId);
    assignmentListener = assignmentListenerFactory.create(subscriberId, subscriptionId, assignmentUpdatedCallback);

    leaderSelector = leaderSelectorFactory.create(lockId,
            String.format("[subscriberId: %s, subscriptionId: %s]", subscriberId, subscriptionId),
            this::onLeaderSelected,
            this::onLeaderRemoved);

    leaderSelector.start();
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
    memberGroupManager = memberGroupManagerFactory.create(subscriberId, subscriptionId, Coordinator.this::onGroupMembersUpdated);
  }

  private void onLeaderRemoved() {
    logger.info("Calling memberGroupManager.stop(), subscriberId : {}, subscriptionId : {}", subscriberId, subscriptionId);
    memberGroupManager.stop();
    logger.info("Called memberGroupManager.stop(), subscriberId : {}, subscriptionId : {}", subscriberId, subscriptionId);

    logger.info("Calling leaderRemoved, subscriberId : {}, subscriptionId : {}", subscriberId, subscriptionId);
    leaderRemoved.run();
    logger.info("Called leaderRemoved, subscriberId : {}, subscriptionId : {}", subscriberId, subscriptionId);
  }

  private void onGroupMembersUpdated(Set<String> expectedGroupMembers) {
    logger.info("Updating group members, expectedGroupMembers : {}, subscriberId : {}, subscriptionId : {}", expectedGroupMembers, subscriberId, subscriptionId);

    try {
      if (!partitionManager.isInitialized()) {
        initializePartitionManager(expectedGroupMembers);
      } else {
        rebalance(expectedGroupMembers);
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return;
    }

    previousGroupMembers = expectedGroupMembers;
    logger.info("Updated group members, subscriberId : {}, subscriptionId : {}", subscriberId, subscriptionId);
  }

  private void initializePartitionManager(Set<String> expectedGroupMembers) {
    logger.info("Initializing partition manager, expectedGroupMembers : {}, subscriberId : {}, subscriptionId : {}", expectedGroupMembers, subscriberId, subscriptionId);
    Map<String, Assignment> assignments = expectedGroupMembers
            .stream()
            .collect(Collectors.toMap(Function.identity(), this::readAssignment));

    partitionManager
            .initialize(assignments)
            .forEach(this::saveAssignment);
  }

  private void rebalance(Set<String> expectedGroupMembers) {
    logger.info("Preparing to rebalance, expectedGroupMembers : {}, subscriberId : {}, subscriptionId : {}", expectedGroupMembers, subscriberId, subscriptionId);

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
