package io.eventuate.tram.spring.events.subscriber;

import io.eventuate.tram.events.subscriber.annotations.EventuateDomainEventHandler;

import java.lang.reflect.Method;

/**
 * Holds metadata about an event handler method annotated with @EventuateDomainEventHandler.
 */
public final class EventuateDomainEventHandlerInfo {
  private final Object target;
  private final EventuateDomainEventHandler eventuateDomainEventHandler;
  private final Method method;

  public EventuateDomainEventHandlerInfo(Object target, EventuateDomainEventHandler eventuateDomainEventHandler, Method method) {
    this.target = target;
    this.eventuateDomainEventHandler = eventuateDomainEventHandler;
    this.method = method;
  }

  public Object getTarget() {
    return target;
  }

  public EventuateDomainEventHandler getEventuateDomainEventHandler() {
    return eventuateDomainEventHandler;
  }

  public Method getMethod() {
    return method;
  }
}