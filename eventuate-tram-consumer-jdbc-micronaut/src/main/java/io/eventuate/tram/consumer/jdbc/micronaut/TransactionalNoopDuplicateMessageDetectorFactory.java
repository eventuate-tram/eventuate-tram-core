package io.eventuate.tram.consumer.jdbc.micronaut;

import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.jdbc.TransactionalNoopDuplicateMessageDetector;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Singleton;

@Factory
public class TransactionalNoopDuplicateMessageDetectorFactory {

  @Singleton
  @Requires(property = "transactional.noop.duplicate.message.detector.factory.enabled")
  public DuplicateMessageDetector duplicateMessageDetector(TransactionTemplate transactionTemplate) {
    return new TransactionalNoopDuplicateMessageDetector(transactionTemplate);
  }
}
