package io.eventuate.tram.spring.events.subscriber;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.annotations.EventuateDomainEventHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Validates that methods annotated with @EventuateDomainEventHandler follow the required conventions:
 * - Must be public
 * - Must have exactly one parameter of type DomainEventEnvelope&lt;T extends DomainEvent&gt;
 * - Must be in a concrete class
 */
public class EventuateDomainEventHandlerMethodValidator {

  public static void validateEventHandlerMethod(EventuateDomainEventHandlerInfo info) {
    if (info == null) {
      throw new EventuateDomainEventHandlerValidationException("EventuateDomainEventHandlerInfo cannot be null");
    }
    validateMethodIsPublic(info.getMethod());
    validateMethodParameters(info.getMethod());
    validateMethodDeclaringClass(info.getMethod());
    validateHandlerAttributes(info.getSubscriberId(), info.getChannel());
  }

  private static void validateMethodIsPublic(Method method) {
    if (!Modifier.isPublic(method.getModifiers())) {
      throw new EventuateDomainEventHandlerValidationException(
              "Event handler method %s must be public".formatted(method));
    }
  }

  private static void validateMethodParameters(Method method) {
    Type[] parameterTypes = method.getGenericParameterTypes();
    if (parameterTypes.length != 1) {
      throw new EventuateDomainEventHandlerValidationException(
              "Event handler method %s must have exactly one parameter".formatted(method));
    }

    if (!(parameterTypes[0] instanceof ParameterizedType)) {
      throw new EventuateDomainEventHandlerValidationException(
              "Event handler method %s parameter must be of type DomainEventEnvelope<T extends DomainEvent>".formatted(method));
    }

    ParameterizedType parameterType = (ParameterizedType) parameterTypes[0];
    if (!DomainEventEnvelope.class.equals(parameterType.getRawType())) {
      throw new EventuateDomainEventHandlerValidationException(
              "Event handler method %s parameter must be of type DomainEventEnvelope<T extends DomainEvent>".formatted(method));
    }

    Type[] typeArguments = parameterType.getActualTypeArguments();
    if (typeArguments.length != 1 || !(typeArguments[0] instanceof Class)) {
      throw new EventuateDomainEventHandlerValidationException(
              "Event handler method %s has invalid event type parameter".formatted(method));
    }

    Class<?> eventClass = (Class<?>) typeArguments[0];
    if (!DomainEvent.class.isAssignableFrom(eventClass)) {
      throw new EventuateDomainEventHandlerValidationException(
              "Event handler method %s event type must extend DomainEvent".formatted(method));
    }
  }

  private static void validateMethodDeclaringClass(Method method) {
    Class<?> declaringClass = method.getDeclaringClass();
    if (Modifier.isAbstract(declaringClass.getModifiers()) || declaringClass.isInterface()) {
      throw new EventuateDomainEventHandlerValidationException(
              "Event handler method %s must be in a concrete class".formatted(method));
    }
  }

  private static void validateHandlerAttributes(String subscriberId, String channel) {
    if (subscriberId == null || subscriberId.trim().isEmpty()) {
      throw new EventuateDomainEventHandlerValidationException("subscriberId must not be empty");
    }
    if (channel == null || channel.trim().isEmpty()) {
      throw new EventuateDomainEventHandlerValidationException("channel must not be empty");
    }
  }
}
