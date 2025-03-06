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
  private final String subscriberId;
  private final String channel;
  private final Method method;
  private final Class<? extends DomainEvent> eventClass;

  private EventuateDomainEventHandlerInfo(Object target, String subscriberId, String channel, Method method, Class<? extends DomainEvent> eventClass) {
    this.target = target;
    this.subscriberId = subscriberId;
    this.channel = channel;
    this.method = method;
    this.eventClass = eventClass;
  }

  @SuppressWarnings("unchecked")
  public static EventuateDomainEventHandlerInfo make(Object target, String subscriberId, String channel, Method method) {
    if (target == null) {
      throw new EventuateDomainEventHandlerValidationException("target cannot be null");
    }
    if (subscriberId == null || subscriberId.trim().isEmpty()) {
      throw new EventuateDomainEventHandlerValidationException("subscriberId cannot be null or empty");
    }
    if (channel == null || channel.trim().isEmpty()) {
      throw new EventuateDomainEventHandlerValidationException("channel cannot be null or empty");
    }
    if (method == null) {
      throw new EventuateDomainEventHandlerValidationException("method cannot be null");
    }

    Class<? extends DomainEvent> eventClass = extractEventClass(method);

    return new EventuateDomainEventHandlerInfo(target, subscriberId, channel, method, eventClass);
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends DomainEvent> extractEventClass(Method method) {
    if (method.getParameterCount() != 1) {
      throw new EventuateDomainEventHandlerValidationException("Event handler method must have exactly one parameter");
    }

    Type parameterType = method.getGenericParameterTypes()[0];
    if (!(parameterType instanceof ParameterizedType)) {
      throw new EventuateDomainEventHandlerValidationException("Event handler method parameter must be parameterized DomainEventEnvelope");
    }

    Type eventType = ((ParameterizedType) parameterType).getActualTypeArguments()[0];
    if (!(eventType instanceof Class)) {
      throw new EventuateDomainEventHandlerValidationException("Event type parameter must be a class");
    }

    Class<?> eventClass = (Class<?>) eventType;
    if (!DomainEvent.class.isAssignableFrom(eventClass)) {
      throw new EventuateDomainEventHandlerValidationException("Event type must implement DomainEvent");
    }

    return (Class<? extends DomainEvent>) eventClass;
  }

  public Object getTarget() {
    return target;
  }

  public Method getMethod() {
    return method;
  }

  public String getSubscriberId() {
    return subscriberId;
  }

  public String getChannel() {
    return channel;
  }

  public Class<? extends DomainEvent> getEventClass() {
    return eventClass;
  }

  public Class<?> getTargetClass() {
    return target.getClass();
  }
}
