package io.eventuate.tram.micronaut.spring.jdbc.optimistic.locking;

import io.eventuate.common.jdbc.EventuateTransactionTemplate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TestEntityServiceTransactionTemplate extends AbstractTestEntityService {

  @Inject
  private EventuateTransactionTemplate eventuateTransactionalTemplate;

  @Override
  public Long createTestEntityInTransaction() {
    return eventuateTransactionalTemplate.executeInTransaction(this::createTestEntity);
  }

  @Override
  public void incDataInTransaction(Long entityId) {
    eventuateTransactionalTemplate.executeInTransaction(() -> {
      incData(entityId);
      return null;
    });
  }

  @Override
  public long getDataInTransaction(Long entityId) {
    return eventuateTransactionalTemplate.executeInTransaction(() -> {
      return getData(entityId);
    });
  }
}
