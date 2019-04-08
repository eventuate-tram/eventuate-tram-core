package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.consumer.common.coordinator.GroupMember;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class RedisGroupMember implements GroupMember {

  private RedisTemplate<String, String> redisTemplate;
  private String memberId;
  private long ttlInMilliseconds;
  private String groupKey;
  private String groupMemberKey;
  private Timer timer = new Timer();

  public RedisGroupMember(RedisTemplate<String, String> redisTemplate,
                          String groupId,
                          String memberId,
                          long ttlInMilliseconds) {

    this.redisTemplate = redisTemplate;
    this.memberId = memberId;
    this.ttlInMilliseconds = ttlInMilliseconds;

    groupKey = RedisKeyUtil.keyForMemberGroupSet(groupId);
    groupMemberKey = RedisKeyUtil.keyForGroupMember(groupId, memberId);

    createOrUpdateGroupMember();
    addMemberToGroup();
    scheduleGroupMemberTtlRefresh();
  }

  @Override
  public void remove() {
    stopTtlRefreshing();

    redisTemplate.opsForSet().remove(groupKey, memberId);
    redisTemplate.delete(groupMemberKey);
  }

  void stopTtlRefreshing() {
    timer.cancel();
  }

  private void addMemberToGroup() {
    redisTemplate.opsForSet().add(groupKey, memberId);
  }

  private void scheduleGroupMemberTtlRefresh() {
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        createOrUpdateGroupMember();
      }
    }, 0, ttlInMilliseconds / 2);
  }

  private void createOrUpdateGroupMember() {
    redisTemplate.opsForValue().set(groupMemberKey, memberId, ttlInMilliseconds, TimeUnit.MILLISECONDS);
  }
}
