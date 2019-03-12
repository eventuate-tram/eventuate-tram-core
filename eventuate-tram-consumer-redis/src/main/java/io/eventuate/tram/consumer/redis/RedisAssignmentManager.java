package io.eventuate.tram.consumer.redis;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

public class RedisAssignmentManager {
  private RedisTemplate<String, String> redisTemplate;
  private long assignmentTtlInMilliseconds;

  public RedisAssignmentManager(RedisTemplate<String, String> redisTemplate, long assignmentTtlInMilliseconds) {
    this.redisTemplate = redisTemplate;
    this.assignmentTtlInMilliseconds = assignmentTtlInMilliseconds;
  }

  public Assignment readAssignment(String groupId, String memberId) {
    String assignmentKey = RedisUtil.keyForAssignment(groupId, memberId);
    return JSonMapper.fromJson(redisTemplate.opsForValue().get(assignmentKey), Assignment.class);
  }

  public void createOrUpdateAssignment(String groupId, String memberId, Assignment assignment) {
    String assignmentKey = RedisUtil.keyForAssignment(groupId, memberId);
    redisTemplate.opsForValue().set(assignmentKey, JSonMapper.toJson(assignment), assignmentTtlInMilliseconds, TimeUnit.MILLISECONDS);
  }
}
