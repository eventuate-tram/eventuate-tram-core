package io.eventuate.tram.micronaut.spring.jdbc.optimistic.locking;

import io.micronaut.spring.tx.annotation.Transactional;

import javax.inject.Singleton;

@Singleton
public class TestEntityServiceTransactionAnnotation extends AbstractTestEntityService {


    @Transactional
    @Override
    public Long createTestEntityInTransaction() {
      return createTestEntity();
    }

    @Transactional
    @Override
    public void incDataInTransaction(Long entityId) {
      incData(entityId);
    }

    @Transactional
    @Override
    public long getDataInTransaction(Long entityId) {
      return getData(entityId);
    }
}
