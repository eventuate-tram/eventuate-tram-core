package io.eventuate.tram.reactive.integrationtests.events;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.reactive.integrationtests.IdSupplier;
import io.eventuate.tram.spring.events.publisher.ReactiveDomainEventPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.reactive.TransactionalOperator;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = ReactiveTramEventIntegrationTestConfiguration.class)
@DirtiesContext
public class ReactiveTramEventIntegrationTest {

  @Autowired
  private ReactiveDomainEventPublisher domainEventPublisher;

  @Autowired
  private ReactiveTramTestEventConsumer testEventConsumer;

  @Autowired
  private ReactiveTramAdditionalTestEventConsumer additionalTestEventConsumer;

  @Autowired
  private TransactionalOperator transactionalOperator;

  private String aggregateId;
  private String payload;

  private String additionalAggregateId;
  private String additionalPayload;

  @BeforeEach
  public void init() {
    aggregateId = IdSupplier.get();
    payload = IdSupplier.get();

    additionalAggregateId = IdSupplier.get();
    additionalPayload = IdSupplier.get();
  }

  @Test
  public void shouldSendAndReceiveEvent() throws InterruptedException {
    String id = domainEventPublisher
            .publish(testEventConsumer.getAggregateType(), aggregateId, new TestEvent(payload))
            .cache()
            .as(transactionalOperator::transactional)
            .map(Message::getId)
            .block();

    assertNotNull(id, "should have message id");
    TestEvent event = testEventConsumer.getQueue().poll(10, TimeUnit.SECONDS);

    Assertions.assertEquals(payload, event.getPayload());
  }

  @Test
  public void shouldNotHandleFilteredEvents() throws InterruptedException {
    domainEventPublisher
            .publish(testEventConsumer.getAggregateType(), aggregateId, Collections.singletonList(new TestEvent(payload + "ignored")))
            .block();

    TestEvent event = testEventConsumer.getQueue().poll(5, TimeUnit.SECONDS);

    Assertions.assertNull(event);
  }

  @Test
  public void shouldSendAndReceiveEventUsingEventBuilder() throws InterruptedException {
    List<Message> messages = domainEventPublisher
            .aggregateType(testEventConsumer.getAggregateType())
            .aggregateId(aggregateId)
            .event(new TestEvent(payload))
            .publish()
            .block();

    Assertions.assertEquals(1, messages.size());

    TestEvent event = testEventConsumer.getQueue().poll(10, TimeUnit.SECONDS);

    Assertions.assertEquals(payload, event.getPayload());
  }

  @Test
  public void shouldSendAndReceiveMultipleEvents() throws InterruptedException {
    List<Message> messages = domainEventPublisher
            .aggregateType(testEventConsumer.getAggregateType()).aggregateId(aggregateId).event(new TestEvent(payload))
            .aggregateType(additionalTestEventConsumer.getAggregateType()).aggregateId(additionalAggregateId).event(new AdditionalTestEvent(additionalPayload))
            .publish()
            .block();

    Assertions.assertEquals(2, messages.size());

    TestEvent event = testEventConsumer.getQueue().poll(10, TimeUnit.SECONDS);
    Assertions.assertEquals(payload, event.getPayload());

    AdditionalTestEvent additionalEvent = additionalTestEventConsumer.getQueue().poll(10, TimeUnit.SECONDS);
    Assertions.assertEquals(additionalPayload, additionalEvent.getPayload());
  }
}
