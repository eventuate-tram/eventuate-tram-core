package io.eventuate.tram.consumer.rabbitmq;

import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PartitionManagerTest {

  @Test
  public void checkInitialBalancing() {
    for (int subscriberCount = 1; subscriberCount <= 10; subscriberCount++) {
      for (int partitionCount = 1; partitionCount <= 10; partitionCount++) {

        System.out.println("subscriber count: " + subscriberCount);
        System.out.println("partition count: " + partitionCount);
        System.out.println("---");

        Map<String, Assignment> assignments = new HashMap<>();
        for (int i = 0; i < subscriberCount; i++) {
          assignments.put(String.valueOf(i), new Assignment(ImmutableSet.of("channel"), new HashMap<>()));
        }

        PartitionManager partitionManager = new PartitionManager(partitionCount);

        partitionManager.rebalance(assignments);

        checkPartitionCountAndUniqueness(assignments, "channel", partitionCount);
        checkMinMaxPartitions(assignments, "channel", partitionCount);
      }
    }
  }

  @Test
  public void checkRebalansingByAddingNewSubscribers() {
    for (int initialSubscriberCount = 1; initialSubscriberCount <= 10; initialSubscriberCount++) {
      for (int partitionCount = 1; partitionCount <= 10; partitionCount++) {
        for (int additionalSubscriberCount = 1; additionalSubscriberCount <= 10; additionalSubscriberCount++) {

          System.out.println("subscriber count: " + initialSubscriberCount);
          System.out.println("partition count: " + partitionCount);
          System.out.println("additional subscriber count: " + initialSubscriberCount);
          System.out.println("---");

          Map<String, Assignment> assignments = new HashMap<>();
          for (int i = 0; i < initialSubscriberCount; i++) {
            assignments.put(String.valueOf(i), new Assignment(ImmutableSet.of("channel"), new HashMap<>()));
          }

          PartitionManager partitionManager = new PartitionManager(partitionCount);

          partitionManager.rebalance(assignments);

          for (int i = 0; i < additionalSubscriberCount; i++) {
            assignments.put(String.valueOf(initialSubscriberCount + i), new Assignment(ImmutableSet.of("channel"), new HashMap<>()));
          }

          partitionManager.rebalance(assignments);

          checkPartitionCountAndUniqueness(assignments, "channel", partitionCount);
          checkMinMaxPartitions(assignments, "channel", partitionCount);
        }
      }
    }
  }

  @Test
  public void checkRebalansingByRemovingSubscribers() {
    for (int initialSubscriberCount = 2; initialSubscriberCount <= 10; initialSubscriberCount++) {
      for (int partitionCount = 1; partitionCount <= 10; partitionCount++) {
        for (int subscribersToRemove = 1; subscribersToRemove < initialSubscriberCount; subscribersToRemove++) {

          System.out.println("subscriber count: " + initialSubscriberCount);
          System.out.println("partition count: " + partitionCount);
          System.out.println("subscribers to remove: " + initialSubscriberCount);
          System.out.println("---");

          Map<String, Assignment> assignments = new HashMap<>();
          for (int i = 0; i < initialSubscriberCount; i++) {
            assignments.put(String.valueOf(i), new Assignment(ImmutableSet.of("channel"), new HashMap<>()));
          }

          PartitionManager partitionManager = new PartitionManager(partitionCount);

          partitionManager.rebalance(assignments);

          for (int i = 0; i < subscribersToRemove; i++) {
            assignments.remove(assignments.keySet().stream().findAny().get());
          }

          partitionManager.rebalance(assignments);

          checkPartitionCountAndUniqueness(assignments, "channel", partitionCount);
          checkMinMaxPartitions(assignments, "channel", partitionCount);
        }
      }
    }
  }

  @Test
  public void checkRebalansingByAddingAndRemovingSubscribers() {
    for (int initialSubscriberCount = 1; initialSubscriberCount <= 10; initialSubscriberCount++) {
      for (int partitionCount = 1; partitionCount <= 10; partitionCount++) {
        for (int additionalSubscriberCount = 1; additionalSubscriberCount <= 10; additionalSubscriberCount++) {
          for (int subscribersToRemove = 1; subscribersToRemove < initialSubscriberCount; subscribersToRemove++) {


            System.out.println("subscriber count: " + initialSubscriberCount);
            System.out.println("partition count: " + partitionCount);
            System.out.println("additional subscriber count: " + initialSubscriberCount);
            System.out.println("subscribers to remove: " + initialSubscriberCount);
            System.out.println("---");

            Map<String, Assignment> assignments = new HashMap<>();
            for (int i = 0; i < initialSubscriberCount; i++) {
              assignments.put(String.valueOf(i), new Assignment(ImmutableSet.of("channel"), new HashMap<>()));
            }

            PartitionManager partitionManager = new PartitionManager(partitionCount);

            partitionManager.rebalance(assignments);

            for (int i = 0; i < subscribersToRemove; i++) {
              assignments.remove(assignments.keySet().stream().findAny().get());
            }

            for (int i = 0; i < additionalSubscriberCount; i++) {
              assignments.put(String.valueOf(initialSubscriberCount + i), new Assignment(ImmutableSet.of("channel"), new HashMap<>()));
            }

            partitionManager.rebalance(assignments);

            checkPartitionCountAndUniqueness(assignments, "channel", partitionCount);
            checkMinMaxPartitions(assignments, "channel", partitionCount);
          }
        }
      }
    }
  }

  private void checkPartitionCountAndUniqueness(Map<String, Assignment> assignments, String channel, int partitionCount) {
    Set<Integer> allPartitions = new HashSet<>();

    assignments.values().forEach(assignment -> {
      Set<Integer> partitionOfCurrentAssignment = assignment.getPartitionAssignmentsByChannel().get(channel);
      partitionOfCurrentAssignment.forEach(partition -> Assert.assertFalse(allPartitions.contains(partition)));
      allPartitions.addAll(partitionOfCurrentAssignment);
    });

    Assert.assertEquals(partitionCount, allPartitions.size());
  }

  private void checkMinMaxPartitions(Map<String, Assignment> assignments, String channel, int partitionCount) {
    int minPartitions = partitionCount / assignments.size();
    int maxPartitions = minPartitions + 1;

    System.out.println("min partitions: " + minPartitions);
    System.out.println("max partitions: " + maxPartitions);
    System.out.println("~~~");

    assignments.values().forEach(assignment -> {
      int partitions = assignment.getPartitionAssignmentsByChannel().get(channel).size();
      System.out.println("partitions: " + partitions);

      Assert.assertTrue(partitions >= partitionCount / assignments.size());
      Assert.assertTrue(partitions <= partitionCount / assignments.size() + 1);
    });
  }
}
