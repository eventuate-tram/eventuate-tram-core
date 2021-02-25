package io.eventuate.tram.spring.optimisticlocking;

import io.eventuate.tram.jdbc.optimistic.locking.common.test.TestEntity;
import io.eventuate.tram.jdbc.optimistic.locking.common.test.TestEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class TestEntityRepositoryImpl implements TestEntityRepository {

  @Autowired
  private TestEntitySpringRepository testEntitySpringRepository;

  @Override
  public TestEntity find(Long entityId) {
    return testEntitySpringRepository.findById(entityId).orElse(null);
  }

  @Override
  public void persist(TestEntity testEntity) {
    testEntitySpringRepository.save(testEntity);
  }
}
