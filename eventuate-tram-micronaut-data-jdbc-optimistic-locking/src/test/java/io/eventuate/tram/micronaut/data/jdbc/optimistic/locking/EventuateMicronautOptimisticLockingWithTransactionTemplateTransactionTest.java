package io.eventuate.tram.micronaut.data.jdbc.optimistic.locking;

import io.eventuate.tram.jdbc.optimistic.locking.common.test.AbstractTestEntityService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

@MicronautTest(transactional = false)
public class EventuateMicronautOptimisticLockingWithTransactionTemplateTransactionTest extends AbstractEventuateMicronautOptimisticLockingTest {

  @Inject
  private TestEntityServiceTransactionTemplate testEntityService;

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