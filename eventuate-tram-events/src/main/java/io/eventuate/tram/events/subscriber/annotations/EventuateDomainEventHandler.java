package io.eventuate.tram.events.subscriber.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for configuring event handlers in Eventuate Tram.
 * Methods annotated with @EventuateDomainEventHandler will be automatically registered
 * as event handlers during application startup.
 *
 * The annotated method must be public and follow Eventuate Tram's event handler conventions.
 * The event type is inferred from the method's parameter type.
 *
 * Example usage:
 * {@code
 *   @EventuateDomainEventHandler(
 *     subscriberId = "orderService",
 *     channel = "customerChannel"
 *   )
 *   public void handleCustomerEvent(CustomerEvent event) {
 *     // Handle the event
 *   }
 * }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventuateDomainEventHandler {

  /**
   * Identifier for a logical group of event handlers.
   * Methods with the same subscriberId will be grouped and used to create a DomainEventDispatcher.
   *
   * @return the subscriber ID
   */
  String subscriberId();

  /**
   * The channel (event source) from which to read events.
   *
   * @return the channel name
   */
  String channel();
}