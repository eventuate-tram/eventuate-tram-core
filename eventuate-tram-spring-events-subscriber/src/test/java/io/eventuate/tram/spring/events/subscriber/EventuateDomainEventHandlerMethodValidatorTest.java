package io.eventuate.tram.spring.events.subscriber;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.annotations.EventuateDomainEventHandler;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.fail;

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

  @Test
  public void shouldValidateCorrectHandler() throws Exception {
    Method method = ValidHandler.class.getMethod("handleEvent", DomainEventEnvelope.class);
    ValidHandler handler = new ValidHandler();
    EventuateDomainEventHandlerInfo info = EventuateDomainEventHandlerInfo.make(handler, "test", "test", method);

    try {
      EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(info);
    } catch (Exception e) {
      fail("Should not throw exception: " + e.getMessage());
    }
  }

  @Test(expected = EventuateDomainEventHandlerValidationException.class)
  public void shouldRejectNonPublicHandler() throws Exception {
    Method method = NonPublicHandler.class.getDeclaredMethod("handleEvent", DomainEventEnvelope.class);
    NonPublicHandler handler = new NonPublicHandler();
    EventuateDomainEventHandlerInfo info = EventuateDomainEventHandlerInfo.make(handler, "test", "test", method);

    EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(info);
  }

  @Test(expected = EventuateDomainEventHandlerValidationException.class)
  public void shouldRejectWrongParameterType() throws Exception {
    Method method = WrongParameterTypeHandler.class.getMethod("handleEvent", TestEvent.class);
    WrongParameterTypeHandler handler = new WrongParameterTypeHandler();
    EventuateDomainEventHandlerInfo info = EventuateDomainEventHandlerInfo.make(handler, "test", "test", method);

    EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(info);
  }

  @Test(expected = EventuateDomainEventHandlerValidationException.class)
  public void shouldRejectRawType() throws Exception {
    Method method = RawTypeHandler.class.getMethod("handleEvent", DomainEventEnvelope.class);
    RawTypeHandler handler = new RawTypeHandler();
    EventuateDomainEventHandlerInfo info = EventuateDomainEventHandlerInfo.make(handler, "test", "test", method);

    EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(info);
  }

  @Test(expected = EventuateDomainEventHandlerValidationException.class)
  public void shouldRejectEmptySubscriberId() throws Exception {
    Method method = ValidHandler.class.getMethod("handleEvent", DomainEventEnvelope.class);
    ValidHandler handler = new ValidHandler();
    EventuateDomainEventHandlerInfo info = EventuateDomainEventHandlerInfo.make(handler, "", "test", method);

    EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(info);
  }

  @Test(expected = EventuateDomainEventHandlerValidationException.class)
  public void shouldRejectEmptyChannel() throws Exception {
    Method method = ValidHandler.class.getMethod("handleEvent", DomainEventEnvelope.class);
    ValidHandler handler = new ValidHandler();
    EventuateDomainEventHandlerInfo info = EventuateDomainEventHandlerInfo.make(handler, "test", "", method);

    EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(info);
  }
}
