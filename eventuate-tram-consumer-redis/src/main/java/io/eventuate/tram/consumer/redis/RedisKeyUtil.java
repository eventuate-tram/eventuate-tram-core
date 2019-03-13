package io.eventuate.tram.consumer.redis;

public class RedisKeyUtil {
  public static String keyForMemberGroupSet(String groupId) {
    return String.format("eventuate-tram:redis:group:members:%s", groupId);
  }

  public static String keyForGroupMember(String groupId, String memberId) {
    return String.format("eventuate-tram:redis:group:member:%s:%s", groupId, memberId);
  }

  public static String keyForAssignment(String groupId, String memberId) {
    return String.format("eventuate-tram:redis:group:member:assignment:%s:%s", groupId, memberId);
  }

  public static String keyForLeaderLock(String groupId) {
    return String.format("eventuate-tram:redis:leader:lock:%s", groupId);
  }
}
