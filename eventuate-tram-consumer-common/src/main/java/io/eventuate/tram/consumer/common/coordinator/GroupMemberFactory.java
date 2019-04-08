package io.eventuate.tram.consumer.common.coordinator;

public interface GroupMemberFactory {
  GroupMember create(String groupId, String memberId);
}
