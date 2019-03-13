package io.eventuate.tram.consumer.redis;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RedisLeaderSelector {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private Runnable leaderSelectedCallback;
  private long lockTimeInMilliseconds;

  private RLock lock;
  private boolean locked = false;
  private Timer timer = new Timer();
  private volatile boolean stopping = false;
  private volatile boolean stoppingRefreshing = false;
  private CountDownLatch stopCountDownLatch = new CountDownLatch(1);

  public RedisLeaderSelector(RedissonClient redissonClient,
                             String groupId,
                             long lockTimeInMilliseconds,
                             Runnable leaderSelectedCallback) {

    this.leaderSelectedCallback = leaderSelectedCallback;
    this.lockTimeInMilliseconds = lockTimeInMilliseconds;

    lock = redissonClient.getLock(RedisKeyUtil.keyForLeaderLock(groupId));

    scheduleLocking();
  }

  void stopRefreshing() {
    stoppingRefreshing = true;
  }

  public void stop() {
    stopping = true;

    try {
      stopCountDownLatch.await();
    } catch (InterruptedException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private void scheduleLocking() {
    timer.schedule(new TimerTask() {
      @Override
      public void run() {

        if (stopping) {
          handleStop();
          return;
        }

        if (stoppingRefreshing) {
          return;
        }

        tryToLock();
      }
    }, 0, lockTimeInMilliseconds / 2);
  }

  private void tryToLock() {
    try {
      if (lock.tryLock(lockTimeInMilliseconds / 4, lockTimeInMilliseconds, TimeUnit.MILLISECONDS)) {
        if (!locked) {
          locked = true;
          leaderSelectedCallback.run();
        }
      } else {
        locked = false;
      }
    } catch (InterruptedException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private void handleStop() {
    if (locked && !stoppingRefreshing) {
      lock.unlock();
    }

    stopCountDownLatch.countDown();
    timer.cancel();
  }
}
