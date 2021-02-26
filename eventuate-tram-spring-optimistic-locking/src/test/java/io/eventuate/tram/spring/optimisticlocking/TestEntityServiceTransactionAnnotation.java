package io.eventuate.tram.spring.optimisticlocking;

import io.eventuate.tram.jdbc.optimistic.locking.common.test.AbstractTestEntityService;
import io.eventuate.tram.jdbc.optimistic.locking.common.test.TestEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class TestEntityServiceTransactionAnnotation extends AbstractTestEntityService {

  @Autowired
  private TestEntityRepository testEntityRepository;

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

  @Override
  public TestEntityRepository getTestEntityRepository() {
    return testEntityRepository;
  }
}
