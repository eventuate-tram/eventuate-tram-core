package io.eventuate.tram.micronaut.spring.jdbc.optimistic.locking;

import io.micronaut.test.annotation.MicronautTest;

import javax.inject.Inject;

@MicronautTest(transactional = false)
public class EventuateMicronautOptimisticLockingWithTransactionTemplateTransactionTest extends AbstractEventuateMicronautOptimisticLockingTest {

  @Inject
  private TestEntityServiceTransactionTemplate testEntityService;

  @Override
  protected AbstractTestEntityService testEntityService() {
    return testEntityService;
  }
}