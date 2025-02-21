package io.eventuate.tram.spring.events.subscriber;

/**
 * Exception thrown when a method annotated with @EventuateDomainEventHandler
 * does not meet the required conventions.
 */
public class EventuateDomainEventHandlerValidationException extends RuntimeException {
  public EventuateDomainEventHandlerValidationException(String message) {
    super(message);
  }

  public EventuateDomainEventHandlerValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}