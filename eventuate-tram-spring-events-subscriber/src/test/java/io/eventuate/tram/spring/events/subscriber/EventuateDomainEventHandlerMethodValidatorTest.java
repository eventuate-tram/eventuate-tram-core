package io.eventuate.tram.spring.events.subscriber;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.annotations.EventuateDomainEventHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

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

  @Test
  public void shouldRejectNonPublicHandler() throws Exception {
    assertThrows(EventuateDomainEventHandlerValidationException.class, () -> {
      Method method = NonPublicHandler.class.getDeclaredMethod("handleEvent", DomainEventEnvelope.class);
      NonPublicHandler handler = new NonPublicHandler();
      EventuateDomainEventHandlerInfo info = EventuateDomainEventHandlerInfo.make(handler, "test", "test", method);

      EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(info);
    });
  }

  @Test
  public void shouldRejectWrongParameterType() throws Exception {
    assertThrows(EventuateDomainEventHandlerValidationException.class, () -> {
      Method method = WrongParameterTypeHandler.class.getMethod("handleEvent", TestEvent.class);
      WrongParameterTypeHandler handler = new WrongParameterTypeHandler();
      EventuateDomainEventHandlerInfo info = EventuateDomainEventHandlerInfo.make(handler, "test", "test", method);

      EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(info);
    });
  }

  @Test
  public void shouldRejectRawType() throws Exception {
    assertThrows(EventuateDomainEventHandlerValidationException.class, () -> {
      Method method = RawTypeHandler.class.getMethod("handleEvent", DomainEventEnvelope.class);
      RawTypeHandler handler = new RawTypeHandler();
      EventuateDomainEventHandlerInfo info = EventuateDomainEventHandlerInfo.make(handler, "test", "test", method);

      EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(info);
    });
  }

  @Test
  public void shouldRejectEmptySubscriberId() throws Exception {
    assertThrows(EventuateDomainEventHandlerValidationException.class, () -> {
      Method method = ValidHandler.class.getMethod("handleEvent", DomainEventEnvelope.class);
      ValidHandler handler = new ValidHandler();
      EventuateDomainEventHandlerInfo info = EventuateDomainEventHandlerInfo.make(handler, "", "test", method);

      EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(info);
    });
  }

  @Test
  public void shouldRejectEmptyChannel() throws Exception {
    assertThrows(EventuateDomainEventHandlerValidationException.class, () -> {
      Method method = ValidHandler.class.getMethod("handleEvent", DomainEventEnvelope.class);
      ValidHandler handler = new ValidHandler();
      EventuateDomainEventHandlerInfo info = EventuateDomainEventHandlerInfo.make(handler, "test", "", method);

      EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(info);
    });
  }
}
