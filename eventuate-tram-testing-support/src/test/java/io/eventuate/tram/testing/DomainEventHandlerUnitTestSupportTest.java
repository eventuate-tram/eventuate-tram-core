package io.eventuate.tram.testing;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.DomainEventHandlers;
import io.eventuate.tram.events.subscriber.DomainEventHandlersBuilder;
import org.junit.Test;

import static io.eventuate.tram.testing.DomainEventHandlerUnitTestSupport.given;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

public class DomainEventHandlerUnitTestSupportTest {

  @Test
  public void shouldUnitTest() {
    MyEventHandlers eventHandlers = spy(MyEventHandlers.class);

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
        expectEventHandlerInvoked(eventHandlers, MyEventHandlers::myEventHandler, (dee) -> {
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