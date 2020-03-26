package io.eventuate.tram.micronaut.spring.jdbc.optimistic.locking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

abstract public class AbstractEventuateMicronautOptimisticLockingTest {

  private static final int UPDATE_THREADS = 3;

  @Inject
  private OptimisticLockingDecorator optimisticLockingDecorator;

  private AtomicInteger attempts = new AtomicInteger(0);

  private CountDownLatch countDownLatch = new CountDownLatch(UPDATE_THREADS);

  protected abstract AbstractTestEntityService testEntityService();

  @Test
  public void shouldRetryOnLockException() throws InterruptedException {

    long testEntityId = testEntityService().createTestEntityInTransaction();

    for (int i = 0; i < UPDATE_THREADS; i++) updateEntity(testEntityId);

    countDownLatch.await();

    Assertions.assertTrue(attempts.get() > UPDATE_THREADS);
    Assertions.assertEquals(UPDATE_THREADS, testEntityService().getDataInTransaction(testEntityId));
  }

  private void updateEntity(Long testEntityId) {
    new Thread(() -> {

      optimisticLockingDecorator.accept(null, subscriberIdAndMessage -> {
        attempts.incrementAndGet();

        testEntityService().incDataInTransaction(testEntityId);
      });

      countDownLatch.countDown();

    }).start();
  }
}