package io.eventuate.tram.consumer.redis;

public class RedisUtil {
  public static String keyForMemberGroupSet(String groupId) {
    return String.format("eventuate-tram:redis:group:members:%s", groupId);
  }

  public static String keyForGroupMember(String groupId, String memberId) {
    return String.format("eventuate-tram:redis:group:member:%s:%s", groupId, memberId);
  }

  public static String keyForAssignment(String groupId, String memberId) {
    return String.format("eventuate-tram:redis:group:member:assignment:%s:%s", groupId, memberId);
  }
}
