package io.eventuate.tram.jdbc.optimistic.locking.common.test;

import io.eventuate.tram.consumer.common.MessageHandlerDecorator;
import org.junit.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractEventuateOptimisticLockingTest {

  private static final int UPDATE_THREADS = 3;

  private AtomicInteger attempts = new AtomicInteger(0);

  private CountDownLatch countDownLatch = new CountDownLatch(UPDATE_THREADS);

  protected abstract AbstractTestEntityService testEntityService();

  public void shouldRetryOnLockException() throws InterruptedException {

    long testEntityId = testEntityService().createTestEntityInTransaction();

    for (int i = 0; i < UPDATE_THREADS; i++) updateEntity(testEntityId);

    countDownLatch.await();

    Assert.assertTrue(attempts.get() > UPDATE_THREADS);
    Assert.assertEquals(UPDATE_THREADS, testEntityService().getDataInTransaction(testEntityId));
  }

  public abstract MessageHandlerDecorator getOptimisticLockingDecorator();

  private void updateEntity(Long testEntityId) {
    new Thread(() -> {

      getOptimisticLockingDecorator().accept(null, subscriberIdAndMessage -> {
        attempts.incrementAndGet();

        testEntityService().incDataInTransaction(testEntityId);
      });

      countDownLatch.countDown();

    }).start();
  }
}