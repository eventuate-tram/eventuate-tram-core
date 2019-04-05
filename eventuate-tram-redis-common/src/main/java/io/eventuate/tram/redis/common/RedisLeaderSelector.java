package io.eventuate.tram.redis.common;

import io.eventuate.coordination.leadership.EventuateLeaderSelector;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RedisLeaderSelector implements EventuateLeaderSelector {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private RedissonClients redissonClients;
  private String lockId;
  private String leaderId;
  private long lockTimeInMilliseconds;
  private Runnable leaderSelectedCallback;
  private Runnable leaderRemovedCallback;
  private RedissonRedLock lock;
  private volatile boolean locked = false;
  private Timer timer = new Timer();
  private volatile boolean stopping = false;
  private volatile boolean stoppingRefreshing = false;
  private CountDownLatch stopCountDownLatch = new CountDownLatch(1);
  private Thread leaderThread;

  public RedisLeaderSelector(RedissonClients redissonClients,
                             String lockId,
                             long lockTimeInMilliseconds,
                             Runnable leaderSelectedCallback,
                             Runnable leaderRemovedCallback) {

    this(redissonClients,
            lockId,
            UUID.randomUUID().toString(),
            lockTimeInMilliseconds,
            leaderSelectedCallback,
            leaderRemovedCallback);
  }

  public RedisLeaderSelector(RedissonClients redissonClients,
                             String lockId,
                             String leaderId,
                             long lockTimeInMilliseconds,
                             Runnable leaderSelectedCallback,
                             Runnable leaderRemovedCallback) {

    this.lockId = lockId;
    this.leaderId = leaderId;
    this.redissonClients = redissonClients;
    this.lockTimeInMilliseconds = lockTimeInMilliseconds;
    this.leaderSelectedCallback = leaderSelectedCallback;
    this.leaderRemovedCallback = leaderRemovedCallback;
  }

  @Override
  public void start() {
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
            .map(rc -> rc.getLock(lockId))
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
          leaderSelected();
        }
      } else if (locked){
        leaderThread.interrupt();
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  private void handleStop() {
    if (locked) {
      leaderThread.interrupt();
    }

    stopCountDownLatch.countDown();
    timer.cancel();
  }

  private void leaderSelected() {
    leaderThread = new Thread(() -> {
      try {
        logger.info("Calling leaderSelectedCallback, leaderId : {}", leaderId);
        leaderSelectedCallback.run();
        logger.info("Called leaderSelectedCallback, leaderId : {}", leaderId);
        Thread.sleep(Long.MAX_VALUE);
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        leaderRemoved();
        lock.unlock();
        locked = false;
      }
    });

    leaderThread.start();
  }

  private void leaderRemoved() {
    try {
      logger.info("Calling leaderRemovedCallback, leaderId : {}", leaderId);
      leaderRemovedCallback.run();
      logger.info("Called leaderRemovedCallback, leaderId : {}", leaderId);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }
}