package io.eventuate.tram.consumer.redis;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class RedisMemberGroupManager {

  private RedisTemplate<String, String> redisTemplate;
  private String groupId;
  private long refreshPeriodInMilliseconds;
  private Consumer<Set<String>> groupMembersUpdatedCallback;

  private Timer timer = new Timer();
  private String groupKey;
  private Set<String> checkedMembers;

  public RedisMemberGroupManager(RedisTemplate<String, String> redisTemplate,
                                 String groupId,
                                 long checkIntervalInMilliseconds,
                                 Consumer<Set<String>> groupMembersUpdatedCallback) {
    this.redisTemplate = redisTemplate;
    this.groupId = groupId;
    this.refreshPeriodInMilliseconds = checkIntervalInMilliseconds;
    this.groupMembersUpdatedCallback = groupMembersUpdatedCallback;

    groupKey = RedisUtil.keyForMemberGroupSet(groupId);
    checkedMembers = getCurrentGroupMembers();

    groupMembersUpdatedCallback.accept(checkedMembers);

    scheduleCheckForChangesInMemberGroup();
  }

  public void stop() {
    timer.cancel();
  }

  private void scheduleCheckForChangesInMemberGroup() {
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        checkChangesInMemberGroup();
      }
    }, 0, refreshPeriodInMilliseconds);
  }

  private void checkChangesInMemberGroup() {
    Set<String> currentMembers = getCurrentGroupMembers();

    new HashSet<>(currentMembers)
            .stream()
            .filter(this::IsGroupMemberExpired)
            .forEach(expiredMemberId -> removeExpiredGroupMember(currentMembers, expiredMemberId));

    if (!checkedMembers.equals(currentMembers)) {
      groupMembersUpdatedCallback.accept(currentMembers);
      checkedMembers = currentMembers;
    }
  }

  private Set<String> getCurrentGroupMembers() {
    return new HashSet<>(redisTemplate.opsForSet().members(groupKey));
  }

  private boolean IsGroupMemberExpired(String memberId) {
    return !redisTemplate.hasKey(RedisUtil.keyForGroupMember(groupId, memberId));
  }

  private void removeExpiredGroupMember(Set<String> currentGroupMembers, String expiredMemberId) {
    redisTemplate.opsForSet().remove(groupKey, expiredMemberId);
    currentGroupMembers.remove(expiredMemberId);
  }
}
