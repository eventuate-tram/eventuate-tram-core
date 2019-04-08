package io.eventuate.tram.consumer.common.coordinator;

import io.eventuate.coordination.leadership.LeaderSelectorFactory;

import java.util.Set;
import java.util.function.Consumer;

public class CoordinatorFactoryImpl implements CoordinatorFactory {
  private AssignmentManager assignmentManager;
  private AssignmentListenerFactory assignmentListenerFactory;
  private MemberGroupManagerFactory memberGroupManagerFactory;
  private LeaderSelectorFactory leaderSelectorFactory;
  private GroupMemberFactory groupMemberFactory;
  private int partitionCount;

  public CoordinatorFactoryImpl(AssignmentManager assignmentManager,
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
                                     String lockId,
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
            lockId,
            leaderSelected,
            leaderRemoved);
  }
}
