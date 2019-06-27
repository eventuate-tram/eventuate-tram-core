package io.eventuate.tram.consumer.common.micronaut;

import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.common.NoopDuplicateMessageDetector;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class TramNoopDuplicateMessageDetectorFactory {

  @Singleton
  public DuplicateMessageDetector duplicateMessageDetector() {
    return new NoopDuplicateMessageDetector();
  }
}
