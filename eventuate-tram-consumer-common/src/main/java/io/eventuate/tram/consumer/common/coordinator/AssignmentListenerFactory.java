package io.eventuate.tram.consumer.common.coordinator;

import java.util.function.Consumer;

public interface AssignmentListenerFactory {
  AssignmentListener create();
}
