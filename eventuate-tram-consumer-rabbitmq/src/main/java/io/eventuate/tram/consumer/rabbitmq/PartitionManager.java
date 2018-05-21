package io.eventuate.tram.consumer.rabbitmq;

import java.util.*;

public class PartitionManager {
  private int partitionCount;

  public PartitionManager(int partitionCount) {
    this.partitionCount = partitionCount;
  }

  public void rebalance(List<Assignment> assignments) {
    Assignment minPartitionAssignment = findAssignmentWithMinPartitions(assignments);
    Assignment maxPartitionAssignment = findAssignmentWithMaxPartitions(assignments);

    List<Integer> notActivePartitions = findNotActivePartitions(assignments);

    notActivePartitions.forEach(partition ->
            findAssignmentWithMinPartitions(assignments).getAssignedPartitions().add(partition));

    while (calculateRebalancedPartitions(maxPartitionAssignment) - calculateRebalancedPartitions(minPartitionAssignment) > 1) {
      int partition = maxPartitionAssignment.getCurrentPartitions().stream().findAny().get();
      maxPartitionAssignment.getResignedPartitions().add(partition);
      minPartitionAssignment.getAssignedPartitions().add(partition);

      minPartitionAssignment = findAssignmentWithMinPartitions(assignments);
      maxPartitionAssignment = findAssignmentWithMaxPartitions(assignments);
    }

    assignments.forEach(assignment -> assignment.setState(AssignmentState.REBALANSING));
  }

  private List<Integer> findNotActivePartitions(List<Assignment> assignments) {
    Set<Integer> activePartitions = new HashSet<>();

    assignments.forEach(a -> {
      Set<Integer> expectedPartitions = new HashSet<>();

      expectedPartitions.addAll(a.getCurrentPartitions());
      expectedPartitions.addAll(a.getAssignedPartitions());
      expectedPartitions.removeAll(a.getResignedPartitions());

      activePartitions.addAll(expectedPartitions);
    });

    List<Integer> notActivePartitions = new ArrayList<>();

    for (int i = 0; i < partitionCount; i++) {
      if (!activePartitions.contains(i)) {
        notActivePartitions.add(i);
      }
    }

    return notActivePartitions;
  }

  private Assignment findAssignmentWithMinPartitions(List<Assignment> assignments) {
    return assignments
            .stream()
            .min(Comparator.comparingInt(this::calculateRebalancedPartitions))
            .get();
  }

  private Assignment findAssignmentWithMaxPartitions(List<Assignment> assignments) {
    return assignments
            .stream()
            .max(Comparator.comparingInt(this::calculateRebalancedPartitions))
            .get();
  }


  private int calculateRebalancedPartitions(Assignment assignment) {
    return assignment.getCurrentPartitions().size() + assignment.getAssignedPartitions().size() - assignment.getResignedPartitions().size();
  }
}
