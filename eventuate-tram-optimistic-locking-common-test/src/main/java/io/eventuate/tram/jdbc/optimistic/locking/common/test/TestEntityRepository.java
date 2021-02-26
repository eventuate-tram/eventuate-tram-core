package io.eventuate.tram.jdbc.optimistic.locking.common.test;

public interface TestEntityRepository {
  TestEntity find(Long entityId);

  void persist(TestEntity testEntity);
}
