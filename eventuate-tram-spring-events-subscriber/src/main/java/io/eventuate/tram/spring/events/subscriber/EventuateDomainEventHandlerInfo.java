package io.eventuate.tram.spring.events.subscriber;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.annotations.EventuateDomainEventHandler;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Holds metadata about an event handler method annotated with @EventuateDomainEventHandler.
 */
public final class EventuateDomainEventHandlerInfo {
  private final Object target;
  private final EventuateDomainEventHandler eventuateDomainEventHandler;
  private final Method method;
  private final Class<? extends DomainEvent> eventClass;

  private EventuateDomainEventHandlerInfo(Object target, EventuateDomainEventHandler eventuateDomainEventHandler, Method method, Class<? extends DomainEvent> eventClass) {
    this.target = target;
    this.eventuateDomainEventHandler = eventuateDomainEventHandler;
    this.method = method;
    this.eventClass = eventClass;
  }

  @SuppressWarnings("unchecked")
  public static EventuateDomainEventHandlerInfo make(Object target, EventuateDomainEventHandler eventuateDomainEventHandler, Method method) {
    if (target == null) {
      throw new IllegalArgumentException("target cannot be null");
    }
    if (eventuateDomainEventHandler == null) {
      throw new IllegalArgumentException("eventuateDomainEventHandler cannot be null");
    }
    if (method == null) {
      throw new IllegalArgumentException("method cannot be null");
    }

    Class<? extends DomainEvent> eventClass = extractEventClass(method);

    return new EventuateDomainEventHandlerInfo(target, eventuateDomainEventHandler, method, eventClass);
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends DomainEvent> extractEventClass(Method method) {
    if (method.getParameterCount() != 1) {
      throw new IllegalArgumentException("Event handler method must have exactly one parameter");
    }

    Type parameterType = method.getGenericParameterTypes()[0];
    if (!(parameterType instanceof ParameterizedType)) {
      throw new IllegalArgumentException("Event handler method parameter must be parameterized DomainEventEnvelope");
    }

    Type eventType = ((ParameterizedType) parameterType).getActualTypeArguments()[0];
    if (!(eventType instanceof Class)) {
      throw new IllegalArgumentException("Event type parameter must be a class");
    }

    Class<?> eventClass = (Class<?>) eventType;
    if (!DomainEvent.class.isAssignableFrom(eventClass)) {
      throw new IllegalArgumentException("Event type must implement DomainEvent");
    }

    return (Class<? extends DomainEvent>) eventClass;
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

  public String getChannel() {
    return eventuateDomainEventHandler.channel();
  }

  public Class<? extends DomainEvent> getEventClass() {
    return eventClass;
  }

  public Class<?> getTargetClass() {
    return target.getClass();
  }
}
