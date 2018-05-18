package io.eventuate.tram.consumer.rabbitmq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    while (maxPartitionAssignment.getCurrentPartitions().size() - minPartitionAssignment.getCurrentPartitions().size() > 1) {
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
      expectedPartitions.removeAll(a.getResignedPartitions());
      expectedPartitions.addAll(a.getAssignedPartitions());

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
            .min((a1, a2) -> {
              int p1 = a1.getCurrentPartitions().size() + a1.getAssignedPartitions().size() - a1.getResignedPartitions().size();
              int p2 = a2.getCurrentPartitions().size() + a2.getAssignedPartitions().size() - a2.getResignedPartitions().size();
              return Integer.compare(p1, p2);
            })
            .get();
  }

  private Assignment findAssignmentWithMaxPartitions(List<Assignment> assignments) {
    return assignments
            .stream()
            .max((a1, a2) -> {
              int p1 = a1.getCurrentPartitions().size() + a1.getAssignedPartitions().size() - a1.getResignedPartitions().size();
              int p2 = a2.getCurrentPartitions().size() + a2.getAssignedPartitions().size() - a2.getResignedPartitions().size();
              return Integer.compare(p1, p2);
            })
            .get();
  }
}
