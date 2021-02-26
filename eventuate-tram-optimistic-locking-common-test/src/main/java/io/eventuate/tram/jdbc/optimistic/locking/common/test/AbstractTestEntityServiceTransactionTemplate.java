package io.eventuate.tram.jdbc.optimistic.locking.common.test;

import io.eventuate.common.jdbc.EventuateTransactionTemplate;

public abstract class AbstractTestEntityServiceTransactionTemplate extends AbstractTestEntityService {

  @Override
  public Long createTestEntityInTransaction() {
    return getEventuateTransactionalTemplate().executeInTransaction(this::createTestEntity);
  }

  @Override
  public void incDataInTransaction(Long entityId) {
    getEventuateTransactionalTemplate().executeInTransaction(() -> {
      incData(entityId);
      return null;
    });
  }

  @Override
  public long getDataInTransaction(Long entityId) {
    return getEventuateTransactionalTemplate().executeInTransaction(() -> {
      return getData(entityId);
    });
  }

  public abstract EventuateTransactionTemplate getEventuateTransactionalTemplate();
}
