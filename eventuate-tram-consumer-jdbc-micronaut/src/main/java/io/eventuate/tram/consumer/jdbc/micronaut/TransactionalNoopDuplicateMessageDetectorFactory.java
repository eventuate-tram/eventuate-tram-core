package io.eventuate.tram.consumer.jdbc.micronaut;

import io.eventuate.common.jdbc.micronaut.EventuateMicronautTransactionManagement;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

import javax.inject.Singleton;

@Factory
public class TransactionalNoopDuplicateMessageDetectorFactory {

  @Singleton
  @Requires(property = "transactional.noop.duplicate.message.detector.factory.enabled")
  public DuplicateMessageDetector duplicateMessageDetector(EventuateMicronautTransactionManagement eventuateMicronautTransactionManagement) {
    return new EventuateMicronautTransactionalNoopDuplicateMessageDetector(eventuateMicronautTransactionManagement);
  }
}
