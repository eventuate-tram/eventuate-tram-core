package io.eventuate.tram.reactive.integrationtests;

import io.eventuate.tram.messaging.common.Message;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReactiveTramEventIntegrationTestConfiguration.class)
@DirtiesContext
public class ReactiveTramEventIntegrationTest {

  @Autowired
  private ReactiveDomainEventPublisher domainEventPublisher;

  @Autowired
  private ReactiveTramTestEventConsumer testEventConsumer;

  @Autowired
  private ReactiveTramAdditionalTestEventConsumer additionalTestEventConsumer;

  private String aggregateId;
  private String payload;

  private String additionalAggregateId;
  private String additionalPayload;

  @Before
  public void init() {
    aggregateId = IdSupplier.get();
    payload = IdSupplier.get();

    additionalAggregateId = IdSupplier.get();
    additionalPayload = IdSupplier.get();
  }

  @Test
  public void shouldSendAndReceiveEvent() throws InterruptedException {
    domainEventPublisher
            .publish(testEventConsumer.getAggregateType(), aggregateId, Collections.singletonList(new TestEvent(payload)))
            .block();

    TestEvent event = testEventConsumer.getQueue().poll(10, TimeUnit.SECONDS);

    Assert.assertEquals(payload, event.getPayload());
  }

  @Test
  public void shouldNotHandleFilteredEvents() throws InterruptedException {
    domainEventPublisher
            .publish(testEventConsumer.getAggregateType(), aggregateId, Collections.singletonList(new TestEvent(payload + "ignored")))
            .block();

    TestEvent event = testEventConsumer.getQueue().poll(5, TimeUnit.SECONDS);

    Assert.assertNull(event);
  }

  @Test
  public void shouldSendAndReceiveEventUsingEventBuilder() throws InterruptedException {
    List<Message> messages = domainEventPublisher
            .aggregateType(testEventConsumer.getAggregateType())
            .aggregateId(aggregateId)
            .event(new TestEvent(payload))
            .publish()
            .block();

    Assert.assertEquals(1, messages.size());

    TestEvent event = testEventConsumer.getQueue().poll(10, TimeUnit.SECONDS);

    Assert.assertEquals(payload, event.getPayload());
  }

  @Test
  public void shouldSendAndReceiveMultipleEvents() throws InterruptedException {
    List<Message> messages = domainEventPublisher
            .aggregateType(testEventConsumer.getAggregateType()).aggregateId(aggregateId).event(new TestEvent(payload))
            .aggregateType(additionalTestEventConsumer.getAggregateType()).aggregateId(additionalAggregateId).event(new AdditionalTestEvent(additionalPayload))
            .publish()
            .block();

    Assert.assertEquals(2, messages.size());

    TestEvent event = testEventConsumer.getQueue().poll(10, TimeUnit.SECONDS);
    Assert.assertEquals(payload, event.getPayload());

    AdditionalTestEvent additionalEvent = additionalTestEventConsumer.getQueue().poll(10, TimeUnit.SECONDS);
    Assert.assertEquals(additionalPayload, additionalEvent.getPayload());
  }
}
