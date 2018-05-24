package io.eventuate.tram.consumer.rabbitmq;

import java.util.*;

public class PartitionManager {
  private int partitionCount;

  public PartitionManager(int partitionCount) {
    this.partitionCount = partitionCount;
  }

  public void rebalance(Map<String, Assignment> assignments) {
    Set<String> channels = new HashSet<>();

    assignments.values().forEach(assignment -> channels.addAll(assignment.getChannels()));

    channels.forEach(channel -> {
      Set<AssignmentDescription> assignmentDescriptions = new HashSet<>();

      assignments.forEach((groupMemberId, assignment) -> {
        if (assignment.getChannels().contains(channel)) {

          assignmentDescriptions
                  .add(new AssignmentDescription(assignment,
                          assignment.getPartitionAssignmentsByChannel().getOrDefault(channel, new HashSet<>()),
                          new HashSet<>(),
                          new HashSet<>()));
        }
      });

      rebalance(assignmentDescriptions);

      assignmentDescriptions.forEach(assignmentDescription -> {
        Set<Integer> expectedPartitions = new HashSet<>(assignmentDescription.getCurrentPartitions());

        expectedPartitions.removeAll(assignmentDescription.getResignedPartitions());
        expectedPartitions.addAll(assignmentDescription.getAssignedPartitions());

        assignmentDescription.getAssignment().getPartitionAssignmentsByChannel().put(channel, expectedPartitions);
      });
    });
  }

  private void rebalance(Set<AssignmentDescription> assignmentDescriptions) {
    Set<Integer> notActivePartitions = findNotActivePartitions(assignmentDescriptions);

    notActivePartitions.forEach(partition ->
            findAssignmentDescriptionWithMinPartitions(assignmentDescriptions).getAssignedPartitions().add(partition));

    AssignmentDescription minPartitionAssignment = findAssignmentDescriptionWithMinPartitions(assignmentDescriptions);
    AssignmentDescription maxPartitionAssignment = findAssignmentDescriptionWithMaxPartitions(assignmentDescriptions);

    while (calculateRebalancedPartitions(maxPartitionAssignment) - calculateRebalancedPartitions(minPartitionAssignment) > 1) {
      Optional<Integer> partitionToReassign = maxPartitionAssignment.getAssignedPartitions().stream().findAny();

      if (partitionToReassign.isPresent()) {
        maxPartitionAssignment.getAssignedPartitions().remove(partitionToReassign.get());
        minPartitionAssignment.getAssignedPartitions().add(partitionToReassign.get());
      } else {
        partitionToReassign = maxPartitionAssignment.getCurrentPartitions().stream().findAny();
        maxPartitionAssignment.getResignedPartitions().add(partitionToReassign.get());
        minPartitionAssignment.getAssignedPartitions().add(partitionToReassign.get());
      }

      minPartitionAssignment = findAssignmentDescriptionWithMinPartitions(assignmentDescriptions);
      maxPartitionAssignment = findAssignmentDescriptionWithMaxPartitions(assignmentDescriptions);
    }
  }

  private Set<Integer> findNotActivePartitions(Set<AssignmentDescription> assignmentDescriptions) {
    Set<Integer> activePartitions = new HashSet<>();

    assignmentDescriptions.forEach(assignmentDescription -> {
      Set<Integer> expectedPartitions = new HashSet<>();

      expectedPartitions.addAll(assignmentDescription.getCurrentPartitions());
      expectedPartitions.addAll(assignmentDescription.getAssignedPartitions());
      expectedPartitions.removeAll(assignmentDescription.getResignedPartitions());

      activePartitions.addAll(expectedPartitions);
    });

    Set<Integer> notActivePartitions = new HashSet<>();

    for (int i = 0; i < partitionCount; i++) {
      if (!activePartitions.contains(i)) {
        notActivePartitions.add(i);
      }
    }

    return notActivePartitions;
  }

  private AssignmentDescription findAssignmentDescriptionWithMinPartitions(Set<AssignmentDescription> assignmentDescriptions) {
    return assignmentDescriptions
            .stream()
            .min(Comparator.comparingInt(this::calculateRebalancedPartitions))
            .get();
  }

  private AssignmentDescription findAssignmentDescriptionWithMaxPartitions(Set<AssignmentDescription> assignmentDescriptions) {
    return assignmentDescriptions
            .stream()
            .max(Comparator.comparingInt(this::calculateRebalancedPartitions))
            .get();
  }


  private int calculateRebalancedPartitions(AssignmentDescription assignmentDescription) {
    return assignmentDescription.getCurrentPartitions().size() + assignmentDescription.getAssignedPartitions().size() - assignmentDescription.getResignedPartitions().size();
  }

  private static class AssignmentDescription {
    private Assignment assignment;
    private Set<Integer> currentPartitions;
    private Set<Integer> assignedPartitions;
    private Set<Integer> resignedPartitions;

    public AssignmentDescription() {
    }

    public AssignmentDescription(Assignment assignment,
                                 Set<Integer> currentPartitions,
                                 Set<Integer> assignedPartitions,
                                 Set<Integer> resignedPartitions) {

      this.assignment = assignment;
      this.currentPartitions = currentPartitions;
      this.assignedPartitions = assignedPartitions;
      this.resignedPartitions = resignedPartitions;
    }

    public Assignment getAssignment() {
      return assignment;
    }

    public Set<Integer> getCurrentPartitions() {
      return currentPartitions;
    }

    public void setCurrentPartitions(Set<Integer> currentPartitions) {
      this.currentPartitions = currentPartitions;
    }

    public Set<Integer> getAssignedPartitions() {
      return assignedPartitions;
    }

    public void setAssignedPartitions(Set<Integer> assignedPartitions) {
      this.assignedPartitions = assignedPartitions;
    }

    public Set<Integer> getResignedPartitions() {
      return resignedPartitions;
    }

    public void setResignedPartitions(Set<Integer> resignedPartitions) {
      this.resignedPartitions = resignedPartitions;
    }
  }
}
