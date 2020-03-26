package io.eventuate.tram.micronaut.spring.jdbc.optimistic.locking;

import io.micronaut.spring.tx.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public abstract class AbstractTestEntityService {

    @PersistenceContext
    private EntityManager entityManager;

    public abstract Long createTestEntityInTransaction();
    public abstract void incDataInTransaction(Long entityId);
    public abstract long getDataInTransaction(Long entityId);

    @Transactional
    public Long createTestEntity() {
        TestEntity testEntity = new TestEntity();
        entityManager.persist(testEntity);
        return testEntity.getId();
    }

    @Transactional
    public void incData(Long entityId) {
        TestEntity testEntity = entityManager.find(TestEntity.class, entityId);
        testEntity.setData(testEntity.getData() + 1);
        sleep();
        entityManager.persist(testEntity);
    }

    @Transactional
    public long getData(Long entityId) {
        TestEntity testEntity = entityManager.find(TestEntity.class, entityId);
        return testEntity.getData();
    }

    private void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
