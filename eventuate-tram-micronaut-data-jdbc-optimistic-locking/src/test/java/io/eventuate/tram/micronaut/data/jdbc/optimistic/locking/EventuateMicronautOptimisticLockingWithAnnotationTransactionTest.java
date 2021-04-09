package io.eventuate.tram.micronaut.data.jdbc.optimistic.locking;

import io.eventuate.tram.jdbc.optimistic.locking.common.test.AbstractTestEntityService;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest(transactional = false)
public class EventuateMicronautOptimisticLockingWithAnnotationTransactionTest extends AbstractEventuateMicronautOptimisticLockingTest {

  @Inject
  private TestEntityServiceTransactionAnnotation testEntityService;

  @Override
  protected AbstractTestEntityService testEntityService() {
    return testEntityService;
  }

  @Override
  @Test
  public void shouldRetryOnLockException() throws InterruptedException {
    super.shouldRetryOnLockException();
  }
}