package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.redis.common.CommonRedisConfiguration;
import io.eventuate.util.test.async.Eventually;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CommonRedisConfiguration.class)
public class GroupManagingTest {

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  private String groupId;
  private String memberId;
  private Set<String> members;

  @Before
  public void init() {
    groupId = UUID.randomUUID().toString();
    memberId = UUID.randomUUID().toString();
    members = Collections.synchronizedSet(new HashSet<>());
  }

  @Test
  public void testGroupMemberAdded() {
    RedisMemberGroupManager groupManager = createRedisMemberGroupManager();
    RedisGroupMember redisGroupMember = createRedisGroupMember();

    Eventually.eventually(this::assertMemberExists);

    redisGroupMember.remove();
    groupManager.stop();
  }

  @Test
  public void testGroupMemberRemoved() {
    RedisGroupMember redisGroupMember = createRedisGroupMember();
    RedisMemberGroupManager groupManager = createRedisMemberGroupManager();

    assertMemberExists();

    redisGroupMember.remove();

    assertMembersEventuallyEmpty();

    groupManager.stop();
  }

  @Test
  public void testGroupMemberExpired() {
    RedisGroupMember redisGroupMember = createRedisGroupMember();
    RedisMemberGroupManager groupManager = createRedisMemberGroupManager();

    assertMemberExists();

    redisGroupMember.stopTtlRefreshing();

    assertMembersEventuallyEmpty();

    groupManager.stop();
  }

  private RedisMemberGroupManager createRedisMemberGroupManager() {
    return new RedisMemberGroupManager(redisTemplate,
            groupId,
            100,
            updatedMembers -> {
              members.clear();
              members.addAll(updatedMembers);
            });
  }

  private RedisGroupMember createRedisGroupMember() {
    return new RedisGroupMember(redisTemplate, groupId, memberId, 1000);
  }

  private void assertMemberExists() {
    Assert.assertEquals(1, members.size());
    Assert.assertTrue(members.contains(memberId));
  }

  private void assertMembersEventuallyEmpty() {
    Eventually.eventually(() -> Assert.assertTrue(members.isEmpty()));
  }
}
