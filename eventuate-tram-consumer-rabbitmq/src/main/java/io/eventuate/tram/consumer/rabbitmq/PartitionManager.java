package io.eventuate.tram.consumer.rabbitmq;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PartitionManager {
  private int partitionCount;
  private boolean initialized;
  private Map<String, Assignment> currentAssignments;

  public PartitionManager(int partitionCount) {
    this.partitionCount = partitionCount;
  }

  public Map<String, Assignment> initialize(Map<String, Assignment> assignments) {
    initialized = true;
    currentAssignments = rebalance(assignments);

    return filterUnchangedAssignments(assignments, currentAssignments);
  }

  public boolean isInitialized() {
    return initialized;
  }

  public Map<String, Assignment> getCurrentAssignments() {
    return currentAssignments;
  }

  public Map<String, Assignment> rebalance(Map<String, Set<String>> addedGroupMembersWithTheirSubscribedChannels,
                                           Set<String> removedGroupMembers) {

    Map<String, Assignment> assignmentsWithoutRemovedMembers = currentAssignments
            .entrySet()
            .stream()
            .filter(groupMemberAndAssignment -> !removedGroupMembers.contains(groupMemberAndAssignment.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    Map<String, Assignment> newMemberAssignments = addedGroupMembersWithTheirSubscribedChannels
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey,
                    groupMemberAndChannels -> new Assignment(groupMemberAndChannels.getValue(), new HashMap<>())));

    Map<String, Assignment> assignmentsToRebalance = new HashMap<>();
    assignmentsToRebalance.putAll(assignmentsWithoutRemovedMembers);
    assignmentsToRebalance.putAll(newMemberAssignments);

    Map<String, Assignment> reassignments = rebalance(assignmentsToRebalance);
    Map<String, Assignment> changedAssignments = filterUnchangedAssignments(currentAssignments, reassignments);
    currentAssignments = reassignments;

    return changedAssignments;
  }

  Map<String, Assignment> rebalance(Map<String, Assignment> assignments) {

    Set<AssignmentDescription> assignmentDescriptions = assignments
            .entrySet()
            .stream()
            .flatMap(this::assignmentToAssignmentDescriptions)
            .collect(Collectors.groupingBy(AssignmentDescription::getChannel))
            .values()
            .stream()
            .map(HashSet::new)
            .map(this::rebalance)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

    Map<String, Map<String, List<AssignmentDescription>>> assignmentDescriptionsByGroupMemberAndChannel = assignmentDescriptions
            .stream()
            .collect(Collectors.groupingBy(AssignmentDescription::getGroupMember))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, this::groupAssignmentDescriptionsByChannel));

    return assignmentDescriptionsByGroupMemberAndChannel
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, this::assignmentDescriptionsByChannelToAssignment));
  }

  private Stream<AssignmentDescription> assignmentToAssignmentDescriptions(Map.Entry<String, Assignment> groupMemberAndAssignment) {
    String groupMember = groupMemberAndAssignment.getKey();
    Assignment assignment = groupMemberAndAssignment.getValue();

    return assignment
            .getChannels()
            .stream()
            .map(channel ->
                    new AssignmentDescription(groupMember,
                            channel,
                            assignment
                                    .getPartitionAssignmentsByChannel()
                                    .getOrDefault(channel, Collections.emptySet())));
  }

  private Map<String, List<AssignmentDescription>> groupAssignmentDescriptionsByChannel(Map.Entry<String, List<AssignmentDescription>> groupMemberAndAssignmentDescriptions) {
    List<AssignmentDescription> assignmentDescriptions = groupMemberAndAssignmentDescriptions.getValue();

    return assignmentDescriptions
            .stream()
            .collect(Collectors.groupingBy(AssignmentDescription::getChannel));
  }

  private Assignment assignmentDescriptionsByChannelToAssignment(Map.Entry<String, Map<String, List<AssignmentDescription>>> groupMemberAndAssignmentDescriptionsByChannel) {
    Map<String, List<AssignmentDescription>> assignmentDescriptionsByChannel = groupMemberAndAssignmentDescriptionsByChannel.getValue();

    Map<String, Set<Integer>> partitionsByChannel = assignmentDescriptionsByChannel
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey,
                    channelAndAssignmentDescriptions -> channelAndAssignmentDescriptions
                            .getValue()
                            .stream()
                            .flatMap(assignmentDescription -> assignmentDescription.calculateExpectedPartitions().stream())
                            .collect(Collectors.toSet())));

    return new Assignment(assignmentDescriptionsByChannel.keySet(), partitionsByChannel);
  }

  private Map<String, Assignment> filterUnchangedAssignments(Map<String, Assignment> originalAssignments,
                                                             Map<String, Assignment> reassignments) {
    return reassignments
            .entrySet()
            .stream()
            .filter(e -> {
              if (originalAssignments.containsKey(e.getKey())) {
                return !originalAssignments.get(e.getKey()).equals(e.getValue());
              }

              return true;
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private Set<AssignmentDescription> rebalance(Set<AssignmentDescription> assignmentDescriptions) {
    Set<AssignmentDescription> rebalancingDescriptions = assignmentDescriptions
            .stream()
            .map(AssignmentDescription::new)
            .collect(Collectors.toSet());

    Set<Integer> notActivePartitions = findNotActivePartitions(rebalancingDescriptions);

    notActivePartitions.forEach(partition ->
            findAssignmentDescriptionWithMinPartitions(rebalancingDescriptions).assignPartition(partition));

    AssignmentDescription minPartitionAssignment = findAssignmentDescriptionWithMinPartitions(rebalancingDescriptions);
    AssignmentDescription maxPartitionAssignment = findAssignmentDescriptionWithMaxPartitions(rebalancingDescriptions);

    while (maxPartitionAssignment.calculateRebalancedPartitions() - minPartitionAssignment.calculateRebalancedPartitions() > 1) {
      minPartitionAssignment.takePartitionFrom(maxPartitionAssignment);

      minPartitionAssignment = findAssignmentDescriptionWithMinPartitions(rebalancingDescriptions);
      maxPartitionAssignment = findAssignmentDescriptionWithMaxPartitions(rebalancingDescriptions);
    }

    return rebalancingDescriptions;
  }

  private Set<Integer> findNotActivePartitions(Set<AssignmentDescription> assignmentDescriptions) {
    Set<Integer> activePartitions = assignmentDescriptions
            .stream()
            .flatMap(assignmentDescription -> {
              Set<Integer> expectedPartitions = new HashSet<>();

              expectedPartitions.addAll(assignmentDescription.getCurrentPartitions());
              expectedPartitions.addAll(assignmentDescription.getAssignedPartitions());
              expectedPartitions.removeAll(assignmentDescription.getResignedPartitions());

              return expectedPartitions.stream();
            })
            .collect(Collectors.toSet());

    return IntStream
            .range(0, partitionCount)
            .boxed()
            .filter(partition -> !activePartitions.contains(partition))
            .collect(Collectors.toSet());
  }

  private AssignmentDescription findAssignmentDescriptionWithMinPartitions(Set<AssignmentDescription> assignmentDescriptions) {
    return assignmentDescriptions
            .stream()
            .min(Comparator.comparingInt(AssignmentDescription::calculateRebalancedPartitions))
            .get();
  }

  private AssignmentDescription findAssignmentDescriptionWithMaxPartitions(Set<AssignmentDescription> assignmentDescriptions) {
    return assignmentDescriptions
            .stream()
            .max(Comparator.comparingInt(AssignmentDescription::calculateRebalancedPartitions))
            .get();
  }

  private static class AssignmentDescription {
    private String groupMember;
    private String channel;
    private Set<Integer> currentPartitions;
    private Set<Integer> assignedPartitions = new HashSet<>();
    private Set<Integer> resignedPartitions = new HashSet<>();

    public AssignmentDescription(AssignmentDescription copy) {
      this(copy.getGroupMember(),
              copy.getChannel(),
              new HashSet<>(copy.currentPartitions));

      assignedPartitions.addAll(copy.assignedPartitions);
      resignedPartitions.addAll(copy.resignedPartitions);
    }

    public AssignmentDescription(String groupMember,
                                 String channel,
                                 Set<Integer> currentPartitions) {

      this.groupMember = groupMember;
      this.channel = channel;
      this.currentPartitions = new HashSet<>(currentPartitions);
    }

    public void assignPartition(int partition) {
      assignedPartitions.add(partition);
    }

    public void takePartitionFrom(AssignmentDescription assignmentDescription) {
      Optional<Integer> partitionToReassign = assignmentDescription.assignedPartitions.stream().findAny();

      if (partitionToReassign.isPresent()) {
        assignmentDescription.assignedPartitions.remove(partitionToReassign.get());
        assignedPartitions.add(partitionToReassign.get());
      } else {
        partitionToReassign = assignmentDescription.calculateExpectedPartitions().stream().findAny();
        assignmentDescription.resignedPartitions.add(partitionToReassign.get());
        assignedPartitions.add(partitionToReassign.get());
      }
    }

    public Set<Integer> calculateExpectedPartitions() {
      HashSet<Integer> expectedPartitions = new HashSet<>(currentPartitions);
      expectedPartitions.removeAll(resignedPartitions);
      expectedPartitions.addAll(assignedPartitions);
      return expectedPartitions;
    }

    public int calculateRebalancedPartitions() {
      return currentPartitions.size() + assignedPartitions.size() - resignedPartitions.size();
    }

    public String getGroupMember() {
      return groupMember;
    }

    public String getChannel() {
      return channel;
    }

    public Set<Integer> getCurrentPartitions() {
      return Collections.unmodifiableSet(currentPartitions);
    }

    public Set<Integer> getAssignedPartitions() {
      return Collections.unmodifiableSet(assignedPartitions);
    }

    public Set<Integer> getResignedPartitions() {
      return Collections.unmodifiableSet(resignedPartitions);
    }
  }
}
