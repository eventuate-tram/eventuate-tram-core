package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.consumer.common.coordinator.CommonLeaderSelector;
import io.eventuate.tram.redis.common.RedissonClients;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RedisLeaderSelector implements CommonLeaderSelector {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private RedissonClients redissonClients;
  private String groupId;
  private long lockTimeInMilliseconds;
  private Runnable leaderSelectedCallback;
  private Runnable leaderRemovedCallback;
  private RedissonRedLock lock;
  private boolean locked = false;
  private Timer timer = new Timer();
  private volatile boolean stopping = false;
  private volatile boolean stoppingRefreshing = false;
  private CountDownLatch stopCountDownLatch = new CountDownLatch(1);

  public RedisLeaderSelector(RedissonClients redissonClients,
                             String groupId,
                             long lockTimeInMilliseconds,
                             Runnable leaderSelectedCallback,
                             Runnable leaderRemovedCallback) {

    this.groupId = groupId;
    this.redissonClients = redissonClients;
    this.lockTimeInMilliseconds = lockTimeInMilliseconds;
    this.leaderSelectedCallback = leaderSelectedCallback;
    this.leaderRemovedCallback = leaderRemovedCallback;

    createRedLock();
    scheduleLocking();
  }


  @Override
  public void stop() {
    stopping = true;

    try {
      stopCountDownLatch.await();
    } catch (InterruptedException e) {
      logger.error(e.getMessage(), e);
    }
  }

  void stopRefreshing() {
    stoppingRefreshing = true;
  }

  private void createRedLock() {

    List<RLock> locks = redissonClients
            .getRedissonClients()
            .stream()
            .map(rc -> rc.getLock(RedisKeyUtil.keyForLeaderLock(groupId)))
            .collect(Collectors.toList());

    lock = new RedissonRedLock(locks.toArray(new RLock[]{}));
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
      } else if (locked) {
        leaderRemovedCallback.run();
        locked = false;
      }
    } catch (InterruptedException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private void handleStop() {
    if (locked && !stoppingRefreshing) {
      lock.unlock();
      leaderRemovedCallback.run();
    }

    stopCountDownLatch.countDown();
    timer.cancel();
  }
}
