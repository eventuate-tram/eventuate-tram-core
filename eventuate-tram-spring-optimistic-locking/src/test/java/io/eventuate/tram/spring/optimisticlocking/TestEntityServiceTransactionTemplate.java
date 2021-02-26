package io.eventuate.tram.spring.optimisticlocking;

import io.eventuate.common.jdbc.EventuateTransactionTemplate;
import io.eventuate.tram.jdbc.optimistic.locking.common.test.AbstractTestEntityServiceTransactionTemplate;
import io.eventuate.tram.jdbc.optimistic.locking.common.test.TestEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class TestEntityServiceTransactionTemplate extends AbstractTestEntityServiceTransactionTemplate {

  @Autowired
  private TestEntityRepository testEntityRepository;

  @Autowired
  private EventuateTransactionTemplate eventuateTransactionalTemplate;

  @Override
  public EventuateTransactionTemplate getEventuateTransactionalTemplate() {
    return eventuateTransactionalTemplate;
  }

  @Override
  public TestEntityRepository getTestEntityRepository() {
    return testEntityRepository;
  }
}
