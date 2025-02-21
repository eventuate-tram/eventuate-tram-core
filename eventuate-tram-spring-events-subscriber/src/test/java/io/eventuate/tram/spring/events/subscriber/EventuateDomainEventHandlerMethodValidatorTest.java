package io.eventuate.tram.spring.events.subscriber;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.annotations.EventuateDomainEventHandler;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EventuateDomainEventHandlerMethodValidatorTest {

  // Test event classes
  public static class TestEvent implements DomainEvent {}
  public static class NonDomainEvent implements DomainEvent {}
  public static class InvalidEvent {}

  // Test handler classes
  public static class ValidHandler {
    @EventuateDomainEventHandler(subscriberId = "test", channel = "test")
    public void handleEvent(DomainEventEnvelope<TestEvent> event) {}
  }

  public static class NonPublicHandler {
    @EventuateDomainEventHandler(subscriberId = "test", channel = "test")
    private void handleEvent(DomainEventEnvelope<TestEvent> event) {}
  }

  public static class WrongParameterTypeHandler {
    @EventuateDomainEventHandler(subscriberId = "test", channel = "test")
    public void handleEvent(TestEvent event) {}
  }

  public static class RawTypeHandler {
    @EventuateDomainEventHandler(subscriberId = "test", channel = "test")
    public void handleEvent(DomainEventEnvelope event) {}
  }

  public static abstract class AbstractHandler {
    @EventuateDomainEventHandler(subscriberId = "test", channel = "test")
    public void handleEvent(DomainEventEnvelope<TestEvent> event) {}
  }

  @Test
  public void shouldValidateCorrectHandler() throws NoSuchMethodException {
    Method method = ValidHandler.class.getMethod("handleEvent", DomainEventEnvelope.class);
    EventuateDomainEventHandler annotation = mock(EventuateDomainEventHandler.class);
    when(annotation.subscriberId()).thenReturn("test");
    when(annotation.channel()).thenReturn("test");

    try {
      EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(method, annotation);
    } catch (Exception e) {
      fail("Should not throw exception: " + e.getMessage());
    }
  }

  @Test(expected = EventuateDomainEventHandlerValidationException.class)
  public void shouldRejectNonPublicHandler() throws NoSuchMethodException {
    Method method = NonPublicHandler.class.getDeclaredMethod("handleEvent", DomainEventEnvelope.class);
    EventuateDomainEventHandler annotation = mock(EventuateDomainEventHandler.class);
    when(annotation.subscriberId()).thenReturn("test");
    when(annotation.channel()).thenReturn("test");

    EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(method, annotation);
  }

  @Test(expected = EventuateDomainEventHandlerValidationException.class)
  public void shouldRejectWrongParameterType() throws NoSuchMethodException {
    Method method = WrongParameterTypeHandler.class.getMethod("handleEvent", TestEvent.class);
    EventuateDomainEventHandler annotation = mock(EventuateDomainEventHandler.class);
    when(annotation.subscriberId()).thenReturn("test");
    when(annotation.channel()).thenReturn("test");

    EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(method, annotation);
  }

  @Test(expected = EventuateDomainEventHandlerValidationException.class)
  public void shouldRejectRawType() throws NoSuchMethodException {
    Method method = RawTypeHandler.class.getMethod("handleEvent", DomainEventEnvelope.class);
    EventuateDomainEventHandler annotation = mock(EventuateDomainEventHandler.class);
    when(annotation.subscriberId()).thenReturn("test");
    when(annotation.channel()).thenReturn("test");

    EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(method, annotation);
  }

  @Test(expected = EventuateDomainEventHandlerValidationException.class)
  public void shouldRejectAbstractClass() throws NoSuchMethodException {
    Method method = AbstractHandler.class.getMethod("handleEvent", DomainEventEnvelope.class);
    EventuateDomainEventHandler annotation = mock(EventuateDomainEventHandler.class);
    when(annotation.subscriberId()).thenReturn("test");
    when(annotation.channel()).thenReturn("test");

    EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(method, annotation);
  }

  @Test(expected = EventuateDomainEventHandlerValidationException.class)
  public void shouldRejectEmptySubscriberId() throws NoSuchMethodException {
    Method method = ValidHandler.class.getMethod("handleEvent", DomainEventEnvelope.class);
    EventuateDomainEventHandler annotation = mock(EventuateDomainEventHandler.class);
    when(annotation.subscriberId()).thenReturn("");
    when(annotation.channel()).thenReturn("test");

    EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(method, annotation);
  }

  @Test(expected = EventuateDomainEventHandlerValidationException.class)
  public void shouldRejectEmptyChannel() throws NoSuchMethodException {
    Method method = ValidHandler.class.getMethod("handleEvent", DomainEventEnvelope.class);
    EventuateDomainEventHandler annotation = mock(EventuateDomainEventHandler.class);
    when(annotation.subscriberId()).thenReturn("test");
    when(annotation.channel()).thenReturn("");

    EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(method, annotation);
  }
}
