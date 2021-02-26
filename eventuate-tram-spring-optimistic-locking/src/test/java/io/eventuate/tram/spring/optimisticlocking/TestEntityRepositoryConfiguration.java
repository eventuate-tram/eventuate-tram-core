package io.eventuate.tram.spring.optimisticlocking;

import io.eventuate.common.spring.jdbc.EventuateTransactionTemplateConfiguration;
import io.eventuate.tram.jdbc.optimistic.locking.common.test.TestEntityRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = "io.eventuate.tram.jdbc.optimistic.locking.common.test")
@Import(EventuateTransactionTemplateConfiguration.class)
public class TestEntityRepositoryConfiguration {
  @Bean
  public TestEntityRepository testEntityRepository() {
    return new TestEntityRepositoryImpl();
  }
}
