package io.eventuate.tram.spring.events.subscriber;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.DomainEventDispatcher;
import io.eventuate.tram.events.subscriber.DomainEventDispatcherFactory;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.DomainEventHandlers;
import io.eventuate.tram.events.subscriber.annotations.EventuateDomainEventHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EventuateDomainEventDispatcherTest {

  private DomainEventDispatcherFactory domainEventDispatcherFactory;
  private EventuateDomainEventDispatcher dispatcher;

  // Test event classes
  public static class TestEvent implements DomainEvent {}

  // Test handler classes
  public static class ValidHandler {
    @EventuateDomainEventHandler(subscriberId = "test", channel = "test")
    public void handleEvent(DomainEventEnvelope<TestEvent> event) {}
  }

  @Before
  public void setUp() {
    domainEventDispatcherFactory = mock(DomainEventDispatcherFactory.class);
    dispatcher = new EventuateDomainEventDispatcher(domainEventDispatcherFactory);
  }

  @Test
  public void shouldRegisterAndStartEventHandler() throws Exception {
    // Given
    ValidHandler handler = new ValidHandler();
    Method method = ValidHandler.class.getMethod("handleEvent", DomainEventEnvelope.class);
    DomainEventDispatcher eventDispatcher = mock(DomainEventDispatcher.class);
    ArgumentCaptor<DomainEventHandlers> handlersCaptor = ArgumentCaptor.forClass(DomainEventHandlers.class);

    when(domainEventDispatcherFactory.make(eq("test"), any(DomainEventHandlers.class)))
        .thenReturn(eventDispatcher);

    // When
    EventuateDomainEventHandler annotation = method.getAnnotation(EventuateDomainEventHandler.class);
    dispatcher.registerHandlerMethod(EventuateDomainEventHandlerInfo.make(handler, annotation.subscriberId(), annotation.channel(), method));
    dispatcher.start();

    // Then
    verify(domainEventDispatcherFactory).make(eq("test"), handlersCaptor.capture());
    DomainEventHandlers handlers = handlersCaptor.getValue();
    assertNotNull(handlers);
    assertTrue(dispatcher.isRunning());
  }

  @Test
  public void shouldStopEventHandler() {
    // Given
    dispatcher.start();

    // When
    dispatcher.stop();

    // Then
    assertFalse(dispatcher.isRunning());
  }

}
