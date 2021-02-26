package io.eventuate.tram.jdbc.optimistic.locking.common.test;

public abstract class AbstractTestEntityService {

  public abstract Long createTestEntityInTransaction();
  public abstract void incDataInTransaction(Long entityId);
  public abstract long getDataInTransaction(Long entityId);

  public Long createTestEntity() {
    TestEntity testEntity = new TestEntity();
    getTestEntityRepository().persist(testEntity);
    return testEntity.getId();
  }

  public void incData(Long entityId) {
    TestEntity testEntity = getTestEntityRepository().find(entityId);
    testEntity.setData(testEntity.getData() + 1);
    sleep();
    getTestEntityRepository().persist(testEntity);
  }

  public long getData(Long entityId) {
    TestEntity testEntity = getTestEntityRepository().find(entityId);
    return testEntity.getData();
  }

  public abstract TestEntityRepository getTestEntityRepository();

  private void sleep() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
