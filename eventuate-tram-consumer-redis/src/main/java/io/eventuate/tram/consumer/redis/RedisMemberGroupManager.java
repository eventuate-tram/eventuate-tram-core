package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.consumer.common.coordinator.MemberGroupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class RedisMemberGroupManager implements MemberGroupManager {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private RedisTemplate<String, String> redisTemplate;
  private String groupId;
  private String memberId;
  private long refreshPeriodInMilliseconds;
  private Consumer<Set<String>> groupMembersUpdatedCallback;

  private Timer timer = new Timer();
  private String groupKey;
  private Set<String> checkedMembers;

  public RedisMemberGroupManager(RedisTemplate<String, String> redisTemplate,
                                 String groupId,
                                 String memberId,
                                 long checkIntervalInMilliseconds,
                                 Consumer<Set<String>> groupMembersUpdatedCallback) {
    this.redisTemplate = redisTemplate;
    this.groupId = groupId;
    this.memberId = memberId;
    this.refreshPeriodInMilliseconds = checkIntervalInMilliseconds;
    this.groupMembersUpdatedCallback = groupMembersUpdatedCallback;

    groupKey = RedisKeyUtil.keyForMemberGroupSet(groupId);
    checkedMembers = getCurrentGroupMembers();

    logger.info("Calling groupMembersUpdatedCallback.accept, members : {}, group: {}, member: {}", checkedMembers, groupId, memberId);
    groupMembersUpdatedCallback.accept(checkedMembers);
    logger.info("Calling groupMembersUpdatedCallback.accept, members : {}, group: {}, member: {}", checkedMembers, groupId, memberId);

    scheduleCheckForChangesInMemberGroup();
  }

  @Override
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
      logger.info("Calling groupMembersUpdatedCallback.accept, members : {}, group: {}, member: {}", currentMembers, groupId, memberId);
      groupMembersUpdatedCallback.accept(currentMembers);
      logger.info("Calling groupMembersUpdatedCallback.accept, members : {}, group: {}, member: {}", currentMembers, groupId, memberId);
      checkedMembers = currentMembers;
    }
  }

  private Set<String> getCurrentGroupMembers() {
    return new HashSet<>(redisTemplate.opsForSet().members(groupKey));
  }

  private boolean IsGroupMemberExpired(String memberId) {
    return !redisTemplate.hasKey(RedisKeyUtil.keyForGroupMember(groupId, memberId));
  }

  private void removeExpiredGroupMember(Set<String> currentGroupMembers, String expiredMemberId) {
    redisTemplate.opsForSet().remove(groupKey, expiredMemberId);
    currentGroupMembers.remove(expiredMemberId);
  }
}
