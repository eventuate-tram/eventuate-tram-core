package io.eventuate.tram.spring.optimisticlocking;

import io.eventuate.tram.jdbc.optimistic.locking.common.test.AbstractTestEntityService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EventuateSpringOptimisticLockingWithAnnotationTransactionTest.Config.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class EventuateSpringOptimisticLockingWithAnnotationTransactionTest extends AbstractEventuateSpringOptimisticLockingTest {

  @Configuration
  @Import({OptimisticLockingDecoratorConfiguration.class, TestEntityRepositoryConfiguration.class})
  public static class Config {
    @Bean
    public TestEntityServiceTransactionAnnotation testEntityServiceTransactionAnnotation() {
      return new TestEntityServiceTransactionAnnotation();
    }
  }

  @Autowired
  private TestEntityServiceTransactionAnnotation testEntityService;

  @Override
  protected AbstractTestEntityService testEntityService() {
    return testEntityService;
  }

  @Override
  @Test
  public void shouldRetryOnLockException() throws InterruptedException {
    super.shouldRetryOnLockException();
  }
}