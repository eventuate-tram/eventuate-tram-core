package io.eventuate.tram.consumer.rabbitmq;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PartitionManagerTest {

  @Test
  public void test2Subscribers2PartitionsResigningPartition() {
    PartitionManager partitionManager = new PartitionManager(2);

    Assignment assignment1 = new Assignment(ImmutableSet.of("channel"), new HashMap<>());
    Assignment assignment2 = new Assignment(ImmutableSet.of("channel"), new HashMap<>(ImmutableMap.of("channel", ImmutableSet.of(0, 1))));

    Map<String, Assignment> assignments = ImmutableMap.of("instance1", assignment1, "instance2", assignment2);

    partitionManager.rebalance(assignments);

    Assert.assertEquals(1, assignment1.getPartitionAssignmentsByChannel().get("channel").size());
    Assert.assertEquals(1, assignment2.getPartitionAssignmentsByChannel().get("channel").size());

    HashSet<Integer> expected = new HashSet<>();
    expected.addAll(assignment1.getPartitionAssignmentsByChannel().get("channel"));
    expected.addAll(assignment2.getPartitionAssignmentsByChannel().get("channel"));

    Assert.assertEquals(ImmutableSet.of(0, 1), expected);
  }

}
