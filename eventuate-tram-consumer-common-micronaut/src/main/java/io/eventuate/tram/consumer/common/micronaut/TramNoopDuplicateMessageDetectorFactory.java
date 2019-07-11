package io.eventuate.tram.consumer.common.micronaut;

import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.common.NoopDuplicateMessageDetector;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

import javax.inject.Singleton;

@Factory
public class TramNoopDuplicateMessageDetectorFactory {

  @Singleton
  @Requires(missingBeans = DuplicateMessageDetector.class)
  public DuplicateMessageDetector duplicateMessageDetector() {
    return new NoopDuplicateMessageDetector();
  }
}
