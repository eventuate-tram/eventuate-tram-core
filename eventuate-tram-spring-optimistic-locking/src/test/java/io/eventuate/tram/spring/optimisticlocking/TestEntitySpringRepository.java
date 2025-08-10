package io.eventuate.tram.spring.optimisticlocking;

import io.eventuate.tram.jdbc.optimistic.locking.common.test.TestEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TestEntitySpringRepository extends JpaRepository<TestEntity, Long> {
}
