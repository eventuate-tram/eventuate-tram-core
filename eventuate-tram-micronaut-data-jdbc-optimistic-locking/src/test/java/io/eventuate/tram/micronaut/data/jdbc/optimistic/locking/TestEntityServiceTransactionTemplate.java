package io.eventuate.tram.micronaut.data.jdbc.optimistic.locking;

import io.eventuate.common.jdbc.EventuateTransactionTemplate;
import io.eventuate.tram.jdbc.optimistic.locking.common.test.AbstractTestEntityServiceTransactionTemplate;
import io.eventuate.tram.jdbc.optimistic.locking.common.test.TestEntityRepository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TestEntityServiceTransactionTemplate extends AbstractTestEntityServiceTransactionTemplate {

  @Inject
  private TestEntityRepository testEntityRepository;

  @Inject
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
