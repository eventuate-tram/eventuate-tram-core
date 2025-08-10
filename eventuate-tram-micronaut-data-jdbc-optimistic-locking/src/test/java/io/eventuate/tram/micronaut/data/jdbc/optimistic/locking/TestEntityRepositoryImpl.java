package io.eventuate.tram.micronaut.data.jdbc.optimistic.locking;

import io.eventuate.tram.jdbc.optimistic.locking.common.test.TestEntity;
import io.eventuate.tram.jdbc.optimistic.locking.common.test.TestEntityRepository;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Singleton
public class TestEntityRepositoryImpl implements TestEntityRepository {
  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public TestEntity find(Long entityId) {
    return entityManager.find(TestEntity.class, entityId);
  }

  @Override
  public void persist(TestEntity testEntity) {
    entityManager.persist(testEntity);
  }
}
