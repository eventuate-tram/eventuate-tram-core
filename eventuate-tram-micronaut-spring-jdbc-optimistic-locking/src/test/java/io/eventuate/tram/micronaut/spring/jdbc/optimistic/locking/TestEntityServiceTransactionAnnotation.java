package io.eventuate.tram.micronaut.spring.jdbc.optimistic.locking;

import io.eventuate.tram.jdbc.optimistic.locking.common.test.AbstractTestEntityService;
import io.eventuate.tram.jdbc.optimistic.locking.common.test.TestEntityRepository;
import io.micronaut.transaction.annotation.TransactionalAdvice;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TestEntityServiceTransactionAnnotation extends AbstractTestEntityService {

  @Inject
  private TestEntityRepository testEntityRepository;

  @TransactionalAdvice
  @Override
  public Long createTestEntityInTransaction() {
    return createTestEntity();
  }

  @TransactionalAdvice
  @Override
  public void incDataInTransaction(Long entityId) {
    incData(entityId);
  }

  @TransactionalAdvice
  @Override
  public long getDataInTransaction(Long entityId) {
      return getData(entityId);
    }

  @Override
  public TestEntityRepository getTestEntityRepository() {
    return testEntityRepository;
  }
}
