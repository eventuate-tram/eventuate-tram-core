package io.eventuate.tram.reactive.integrationtests;

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
public class ReactiveTramEventIntegrationTest {

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
  public void shouldSendAndReceiveEvent() throws InterruptedException {
    domainEventPublisher
            .publish(tramTestEventConsumer.getAggregateType(), aggregateId, Collections.singletonList(new TestEvent(payload)))
            .block();

    TestEvent event = tramTestEventConsumer.getQueue().poll(10, TimeUnit.SECONDS);

    Assert.assertEquals(payload, event.getPayload());
  }

  @Test
  public void shouldNotHandleFilteredEvents() throws InterruptedException {
    domainEventPublisher
            .publish(tramTestEventConsumer.getAggregateType(), aggregateId, Collections.singletonList(new TestEvent(payload + "ignored")))
            .block();

    TestEvent event = tramTestEventConsumer.getQueue().poll(5, TimeUnit.SECONDS);

    Assert.assertNull(event);
  }
}
