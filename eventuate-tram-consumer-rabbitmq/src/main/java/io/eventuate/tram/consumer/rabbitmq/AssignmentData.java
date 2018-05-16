package io.eventuate.tram.consumer.rabbitmq;

import java.util.Collections;
import java.util.Set;

public class AssignmentData {
  private String instanceId;
  private AssignmentState state = AssignmentState.NORMAL;
  private Set<Integer> assignedQueues = Collections.emptySet();
  private Set<Integer> resignedQueues = Collections.emptySet();
  private Set<Integer> currentQueues = Collections.emptySet();

  public AssignmentData() {

  }

  public AssignmentData(String instanceId) {
    this.instanceId = instanceId;
  }

  public AssignmentData(String instanceId,
                        AssignmentState state,
                        Set<Integer> assignedQueues,
                        Set<Integer> resignedQueues,
                        Set<Integer> currentQueues) {
    this.instanceId = instanceId;
    this.state = state;
    this.assignedQueues = assignedQueues;
    this.resignedQueues = resignedQueues;
    this.currentQueues = currentQueues;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String subscriberId) {
    this.instanceId = subscriberId;
  }

  public AssignmentState getState() {
    return state;
  }

  public void setState(AssignmentState state) {
    this.state = state;
  }

  public Set<Integer> getAssignedQueues() {
    return assignedQueues;
  }

  public void setAssignedQueues(Set<Integer> assignedQueues) {
    this.assignedQueues = assignedQueues;
  }

  public Set<Integer> getResignedQueues() {
    return resignedQueues;
  }

  public void setResignedQueues(Set<Integer> resignedQueues) {
    this.resignedQueues = resignedQueues;
  }

  public Set<Integer> getCurrentQueues() {
    return currentQueues;
  }

  public void setCurrentQueues(Set<Integer> currentQueues) {
    this.currentQueues = currentQueues;
  }
}
