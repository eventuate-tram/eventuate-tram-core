package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.consumer.common.coordinator.Assignment;

import java.util.Set;
import java.util.function.Consumer;

public interface RedisCoordinatorFactory {
  Coordinator makeCoordinator(String subscriberId, Set<String> channels, String subscriptionId,
                              Consumer<Assignment> assignmentUpdatedCallback);
}
