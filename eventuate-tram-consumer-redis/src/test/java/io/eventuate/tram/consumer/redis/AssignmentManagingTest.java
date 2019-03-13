package io.eventuate.tram.consumer.redis;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.eventuate.tram.consumer.redis.*;
import io.eventuate.tram.redis.common.CommonRedisConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CommonRedisConfiguration.class)
public class AssignmentManagingTest {

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  private String groupId;
  private String memberId;
  private Assignment assignment;

  @Before
  public void init() {
    groupId = UUID.randomUUID().toString();
    memberId = UUID.randomUUID().toString();
    assignment = createAssignment();
  }

  @Test
  public void testListeningForAssignment() {
    Set<Assignment> assignments = Collections.synchronizedSet(new HashSet<>());

    RedisAssignmentListener redisAssignmentListener = new RedisAssignmentListener(redisTemplate, groupId, memberId, 50, a -> {
      Assert.assertEquals(1, assignments.size());
      Assert.assertTrue(assignments.contains(a));
    });

    RedisAssignmentManager redisAssignmentManager = createRedisAssignmentManager();

    redisAssignmentManager.createOrUpdateAssignment(groupId, memberId, assignment);

    redisAssignmentListener.remove();
  }

  @Test
  public void testWriteReadAssignment() {
    RedisAssignmentManager redisAssignmentManager = createRedisAssignmentManager();

    redisAssignmentManager.createOrUpdateAssignment(groupId, memberId, assignment);

    Assert.assertEquals(assignment, redisAssignmentManager.readAssignment(groupId, memberId));
  }

  private Assignment createAssignment() {
    String channel = UUID.randomUUID().toString();
    return new Assignment(ImmutableSet.of(channel), ImmutableMap.of(channel, ImmutableSet.of(0, 1)));
  }

  private RedisAssignmentManager createRedisAssignmentManager() {
    return new RedisAssignmentManager(redisTemplate, 36000000);
  }
}
