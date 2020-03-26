package io.eventuate.tram.micronaut.spring.jdbc.optimistic.locking;

import io.micronaut.test.annotation.MicronautTest;

import javax.inject.Inject;

@MicronautTest(transactional = false)
public class EventuateMicronautOptimisticLockingWithAnnotationTransactionTest extends AbstractEventuateMicronautOptimisticLockingTest {


  @Inject
  private TestEntityServiceTransactionAnnotation testEntityService;


  @Override
  protected AbstractTestEntityService testEntityService() {
    return testEntityService;
  }
}