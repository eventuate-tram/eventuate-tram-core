package io.eventuate.tram.reactive.integrationtests.events;

import io.eventuate.tram.reactive.integrationtests.IdSupplier;
import io.eventuate.tram.spring.events.publisher.ReactiveDomainEventPublisher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReactiveTramEventIntegrationTestConfiguration.class)
@DirtiesContext
public class ReactiveTramEventTransactionIntegrationTest {

  @Autowired
  private ReactiveDomainEventPublisher domainEventPublisher;

  @Autowired
  private ReactiveTramTestEventConsumer tramTestEventConsumer;

  private String aggregateId;
  private String payload;

  @Before
  public void init() {
    aggregateId = IdSupplier.get();
    payload = IdSupplier.get();
  }

  @Test
  public void shouldRollbackFailedTransactionInsideEventConsumer() throws InterruptedException {
    domainEventPublisher
            .publish(tramTestEventConsumer.getAggregateType(), aggregateId, Collections.singletonList(new TestEventThatInitiatesException(payload)))
            .block();

    //event consumer will try to publish reply (TestEvent), but because of exception it should not be delivered (transaction rollback)
    TestEvent event = tramTestEventConsumer.getQueue().poll(5, TimeUnit.SECONDS);

    Assert.assertNull(event);
  }
}
