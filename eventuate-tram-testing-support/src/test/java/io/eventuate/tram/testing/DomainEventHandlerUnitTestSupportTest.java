package io.eventuate.tram.testing;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.DomainEventHandlers;
import io.eventuate.tram.events.subscriber.DomainEventHandlersBuilder;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static io.eventuate.tram.testing.DomainEventHandlerUnitTestSupport.given;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DomainEventHandlerUnitTestSupportTest {

  @Test
  public void shouldUnitTest() {
    MyEventHandlers eventHandlers = mock(MyEventHandlers.class);

    DomainEventHandlers domainEventHandlers = DomainEventHandlersBuilder
            .forAggregateType("MyAggregate")
            .onEvent(MyEvent.class, eventHandlers::myEventHandler)
            .build();
    given().
        eventHandlers(domainEventHandlers).
        when().
        aggregate("MyAggregate", 101L).
        publishes(new MyEvent()).
        then().
        verify(() -> {
          ArgumentCaptor<DomainEventEnvelope<MyEvent>> arg = ArgumentCaptor.forClass(DomainEventEnvelope.class);
          verify(eventHandlers).myEventHandler(arg.capture());
          DomainEventEnvelope<MyEvent> dee = arg.getValue();
          assertEquals(Long.toString(101L), dee.getAggregateId());
        })
    ;

  }

  interface MyEventHandlers {
    void myEventHandler(DomainEventEnvelope<MyEvent> dee);
  }


  public static class MyEvent implements DomainEvent {
  }
}