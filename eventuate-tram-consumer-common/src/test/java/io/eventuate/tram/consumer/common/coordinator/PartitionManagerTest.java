package io.eventuate.tram.consumer.common.coordinator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PartitionManagerTest {

  @Test
  public void checkInitialBalancing() {
    for (int subscriberCount = 1; subscriberCount <= 10; subscriberCount++) {
      for (int partitionCount = 1; partitionCount <= 10; partitionCount++) {
        PartitionManager partitionManager = new PartitionManager(partitionCount);
        Map<String, Assignment> assignments = createEmptyAssignments(subscriberCount);
        partitionManager.initialize(assignments);

        assertThatEachPartitionEncounteredOnceAndPartitionCountIsCorrect(partitionManager.getCurrentAssignments(), "channel", partitionCount);
        assertThatEachAssignmentHasCorrectPartitionCount(partitionManager.getCurrentAssignments(), "channel", partitionCount);
      }
    }
  }

  @Test
  public void checkRebalancingByAddingNewSubscribers() {
    for (int initialSubscriberCount = 1; initialSubscriberCount <= 10; initialSubscriberCount++) {
      for (int partitionCount = 1; partitionCount <= 10; partitionCount++) {
        for (int additionalSubscriberCount = 1; additionalSubscriberCount <= 10; additionalSubscriberCount++) {
          PartitionManager partitionManager = new PartitionManager(partitionCount);

          Map<String, Assignment> assignments = createEmptyAssignments(initialSubscriberCount);
          partitionManager.initialize(assignments);
          partitionManager.rebalance(createNewGroupMembersWithChannels(additionalSubscriberCount), ImmutableSet.of());

          assertThatEachPartitionEncounteredOnceAndPartitionCountIsCorrect(partitionManager.getCurrentAssignments(), "channel", partitionCount);
          assertThatEachAssignmentHasCorrectPartitionCount(partitionManager.getCurrentAssignments(), "channel", partitionCount);
        }
      }
    }
  }

  @Test
  public void checkRebalancingByRemovingSubscribers() {
    for (int initialSubscriberCount = 2; initialSubscriberCount <= 10; initialSubscriberCount++) {
      for (int partitionCount = 1; partitionCount <= 10; partitionCount++) {
        for (int subscribersToRemove = 1; subscribersToRemove < initialSubscriberCount; subscribersToRemove++) {
          PartitionManager partitionManager = new PartitionManager(partitionCount);

          Map<String, Assignment> assignments = createEmptyAssignments(initialSubscriberCount);
          partitionManager.initialize(assignments);
          partitionManager.rebalance(ImmutableMap.of(), findRemovedGroupMembers(subscribersToRemove, assignments));

          assertThatEachPartitionEncounteredOnceAndPartitionCountIsCorrect(partitionManager.getCurrentAssignments(), "channel", partitionCount);
          assertThatEachAssignmentHasCorrectPartitionCount(partitionManager.getCurrentAssignments(), "channel", partitionCount);
        }
      }
    }
  }

  @Test
  public void checkRebalancingByAddingAndRemovingSubscribers() {
    for (int initialSubscriberCount = 1; initialSubscriberCount <= 10; initialSubscriberCount++) {
      for (int partitionCount = 1; partitionCount <= 10; partitionCount++) {
        for (int additionalSubscriberCount = 1; additionalSubscriberCount <= 10; additionalSubscriberCount++) {
          for (int subscribersToRemove = 1; subscribersToRemove < initialSubscriberCount; subscribersToRemove++) {
            PartitionManager partitionManager = new PartitionManager(partitionCount);

            Map<String, Assignment> assignments = createEmptyAssignments(initialSubscriberCount);
            partitionManager.initialize(assignments);
            partitionManager.rebalance(createNewGroupMembersWithChannels(additionalSubscriberCount), findRemovedGroupMembers(subscribersToRemove, assignments));

            assertThatEachPartitionEncounteredOnceAndPartitionCountIsCorrect(partitionManager.getCurrentAssignments(), "channel", partitionCount);
            assertThatEachAssignmentHasCorrectPartitionCount(partitionManager.getCurrentAssignments(), "channel", partitionCount);
          }
        }
      }
    }
  }

  private Map<String, Assignment> createEmptyAssignments(int count) {
    return IntStream
            .range(0, count)
            .boxed()
            .collect(Collectors.toMap(String::valueOf,
                    value -> new Assignment(ImmutableSet.of("channel"), new HashMap<>())));
  }

  private Map<String, Set<String>> createNewGroupMembersWithChannels(int count) {
    return IntStream
            .range(0, count)
            .boxed()
            .collect(Collectors.toMap(String::valueOf,
                    value -> ImmutableSet.of("channel")));
  }

  private Set<String> findRemovedGroupMembers(int count, Map<String, Assignment> assignments) {
    return IntStream
            .range(0, count)
            .boxed()
            .map(i -> assignments.keySet().stream().findAny().get())
            .collect(Collectors.toSet());
  }

  private void assertThatEachPartitionEncounteredOnceAndPartitionCountIsCorrect(Map<String, Assignment> assignments,
                                                                                String channel,
                                                                                int totalPartitions) {
    Set<Integer> allPartitions = new HashSet<>();

    assignments.values().forEach(assignment -> {
      Set<Integer> partitionOfCurrentAssignment = assignment.getPartitionAssignmentsByChannel().get(channel);
      partitionOfCurrentAssignment.forEach(partition -> Assert.assertFalse(allPartitions.contains(partition)));
      allPartitions.addAll(partitionOfCurrentAssignment);
    });

    Assert.assertEquals(totalPartitions, allPartitions.size());
  }

  private void assertThatEachAssignmentHasCorrectPartitionCount(Map<String, Assignment> assignments,
                                                                String channel,
                                                                int totalPartitions) {
    int minPartitions = totalPartitions / assignments.size();
    int maxPartitions = minPartitions + 1;

    assignments.values().forEach(assignment -> {
      int partitions = assignment.getPartitionAssignmentsByChannel().get(channel).size();

      Assert.assertTrue(partitions >= minPartitions);
      Assert.assertTrue(partitions <= maxPartitions);
    });
  }
}
