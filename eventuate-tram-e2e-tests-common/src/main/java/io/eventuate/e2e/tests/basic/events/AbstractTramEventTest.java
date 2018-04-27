package io.eventuate.e2e.tests.basic.events;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.e2e.tests.basic.events.domain.AccountDebited;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractTramEventTest {

  @Autowired
  private DomainEventPublisher domainEventPublisher;

  @Autowired
  private AbstractTramEventTestConfig config;

  @Autowired
  private TramEventTestEventConsumer tramEventTestEventConsumer;

  @Test
  public void shouldReceiveEvent() throws InterruptedException {
    long uniqueId = config.getUniqueId();

    DomainEvent domainEvent = new AccountDebited(uniqueId);

    domainEventPublisher.publish(config.getAggregateType(), config.getAggregateId(), Collections.singletonList(domainEvent));


    AccountDebited event = tramEventTestEventConsumer.getQueue().poll(5, TimeUnit.SECONDS);

    assertNotNull(event);
    assertEquals(uniqueId, event.getAmount());
  }

}
