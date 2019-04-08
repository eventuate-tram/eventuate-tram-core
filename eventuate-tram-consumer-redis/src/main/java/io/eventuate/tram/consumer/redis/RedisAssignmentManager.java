package io.eventuate.tram.consumer.redis;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.common.coordinator.Assignment;
import io.eventuate.tram.consumer.common.coordinator.AssignmentManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

public class RedisAssignmentManager implements AssignmentManager {
  private RedisTemplate<String, String> redisTemplate;
  private long assignmentTtlInMilliseconds;

  public RedisAssignmentManager(RedisTemplate<String, String> redisTemplate, long assignmentTtlInMilliseconds) {
    this.redisTemplate = redisTemplate;
    this.assignmentTtlInMilliseconds = assignmentTtlInMilliseconds;
  }

  @Override
  public void initializeAssignment(String groupId, String memberId, Assignment assignment) {
    String assignmentKey = RedisKeyUtil.keyForAssignment(groupId, memberId);
    redisTemplate.opsForValue().set(assignmentKey, JSonMapper.toJson(assignment), assignmentTtlInMilliseconds, TimeUnit.MILLISECONDS);
  }

  @Override
  public Assignment readAssignment(String groupId, String memberId) {
    String assignmentKey = RedisKeyUtil.keyForAssignment(groupId, memberId);
    return JSonMapper.fromJson(redisTemplate.opsForValue().get(assignmentKey), Assignment.class);
  }

  @Override
  public void saveAssignment(String groupId, String memberId, Assignment assignment) {
    String assignmentKey = RedisKeyUtil.keyForAssignment(groupId, memberId);
    redisTemplate.opsForValue().set(assignmentKey, JSonMapper.toJson(assignment), assignmentTtlInMilliseconds, TimeUnit.MILLISECONDS);
  }
}
