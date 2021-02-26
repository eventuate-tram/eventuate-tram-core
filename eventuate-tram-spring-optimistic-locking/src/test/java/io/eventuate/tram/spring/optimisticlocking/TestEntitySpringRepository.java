package io.eventuate.tram.spring.optimisticlocking;

import io.eventuate.tram.jdbc.optimistic.locking.common.test.TestEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TestEntitySpringRepository extends PagingAndSortingRepository<TestEntity, Long> {
}
