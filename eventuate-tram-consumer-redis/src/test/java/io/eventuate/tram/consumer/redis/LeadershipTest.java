package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.redis.common.CommonRedisConfiguration;
import io.eventuate.tram.redis.common.RedissonClients;
import io.eventuate.util.test.async.Eventually;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CommonRedisConfiguration.class)
public class LeadershipTest {

  @Autowired
  private RedissonClients redissonClients;

  private String groupId;
  private String memberId;

  @Before
  public void init() {
    groupId = UUID.randomUUID().toString();
    memberId = UUID.randomUUID().toString();
  }

  @Test
  public void testThatCallbackInvokedOnce() throws Exception {
    AtomicInteger callbackInvocationCounter = new AtomicInteger(0);

    RedisLeaderSelector redisLeaderSelector = createLeaderSelector(callbackInvocationCounter);

    Thread.sleep(1000);

    Assert.assertEquals(1, callbackInvocationCounter.get());

    redisLeaderSelector.stop();
  }

  @Test
  public void testThatLeaderChangedWhenStopped() throws Exception {
    AtomicInteger callbackInvocationCounterForLeader1 = new AtomicInteger(0);
    AtomicInteger callbackInvocationCounterForLeader2 = new AtomicInteger(0);

    RedisLeaderSelector redisLeaderSelector1 = createLeaderSelector(callbackInvocationCounterForLeader1);
    RedisLeaderSelector redisLeaderSelector2 = createLeaderSelector(callbackInvocationCounterForLeader2);

    assertLeadershipWasAssignedForOneSelector(callbackInvocationCounterForLeader1, callbackInvocationCounterForLeader2);

    boolean leader1 = callbackInvocationCounterForLeader1.get() == 1;

    if (leader1) {
      redisLeaderSelector1.stop();
    } else {
      redisLeaderSelector2.stop();
    }

    assertLeadershipWasAssignedForBothSelectors(callbackInvocationCounterForLeader1, callbackInvocationCounterForLeader2);

    if (leader1) {
      redisLeaderSelector2.stop();
    } else {
      redisLeaderSelector1.stop();
    }
  }

  @Test
  public void testThatLeaderChangedWhenExpired() throws Exception {
    AtomicInteger callbackInvocationCounterForLeader1 = new AtomicInteger(0);
    AtomicInteger callbackInvocationCounterForLeader2 = new AtomicInteger(0);

    RedisLeaderSelector redisLeaderSelector1 = createLeaderSelector(callbackInvocationCounterForLeader1);
    RedisLeaderSelector redisLeaderSelector2 = createLeaderSelector(callbackInvocationCounterForLeader2);

    assertLeadershipWasAssignedForOneSelector(callbackInvocationCounterForLeader1, callbackInvocationCounterForLeader2);

    boolean leader1 = callbackInvocationCounterForLeader1.get() == 1;

    if (leader1) {
      redisLeaderSelector1.stopRefreshing();
    } else {
      redisLeaderSelector2.stopRefreshing();
    }

    assertLeadershipWasAssignedForBothSelectors(callbackInvocationCounterForLeader1, callbackInvocationCounterForLeader2);

    redisLeaderSelector2.stop();
    redisLeaderSelector1.stop();
  }

  private void assertLeadershipWasAssignedForOneSelector(AtomicInteger invocationCounter1, AtomicInteger invocationCounter2) {
    Eventually.eventually(() -> {
      boolean leader1Condition = invocationCounter1.get() == 1 && invocationCounter2.get() == 0;
      boolean leader2Condition = invocationCounter2.get() == 1 && invocationCounter1.get() == 0;
      Assert.assertTrue(leader1Condition || leader2Condition);
    });
  }

  private void assertLeadershipWasAssignedForBothSelectors(AtomicInteger invocationCounter1, AtomicInteger invocationCounter2) {
    Eventually.eventually(() -> {
      Assert.assertEquals(1, invocationCounter1.get());
      Assert.assertEquals(1, invocationCounter2.get());
    });
  }

  private RedisLeaderSelector createLeaderSelector(AtomicInteger invocationCounter) {
    return new RedisLeaderSelector(redissonClients, groupId, memberId,100, invocationCounter::incrementAndGet, () -> {});
  }
}
