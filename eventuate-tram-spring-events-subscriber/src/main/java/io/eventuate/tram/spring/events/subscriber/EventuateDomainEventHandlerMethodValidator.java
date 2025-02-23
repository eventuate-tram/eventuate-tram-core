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

  public static void validateEventHandlerMethod(Method method, EventuateDomainEventHandler annotation) {
    validateMethodIsPublic(method);
    validateMethodParameters(method);
    validateMethodDeclaringClass(method);
    validateAnnotationAttributes(annotation);
  }

  private static void validateMethodIsPublic(Method method) {
    if (!Modifier.isPublic(method.getModifiers())) {
      throw new EventuateDomainEventHandlerValidationException(
          String.format("Event handler method %s must be public", method));
    }
  }

  private static void validateMethodParameters(Method method) {
    Type[] parameterTypes = method.getGenericParameterTypes();
    if (parameterTypes.length != 1) {
      throw new EventuateDomainEventHandlerValidationException(
          String.format("Event handler method %s must have exactly one parameter", method));
    }

    if (!(parameterTypes[0] instanceof ParameterizedType)) {
      throw new EventuateDomainEventHandlerValidationException(
          String.format("Event handler method %s parameter must be of type DomainEventEnvelope<T extends DomainEvent>", method));
    }

    ParameterizedType parameterType = (ParameterizedType) parameterTypes[0];
    if (!DomainEventEnvelope.class.equals(parameterType.getRawType())) {
      throw new EventuateDomainEventHandlerValidationException(
          String.format("Event handler method %s parameter must be of type DomainEventEnvelope<T extends DomainEvent>", method));
    }

    Type[] typeArguments = parameterType.getActualTypeArguments();
    if (typeArguments.length != 1 || !(typeArguments[0] instanceof Class)) {
      throw new EventuateDomainEventHandlerValidationException(
          String.format("Event handler method %s has invalid event type parameter", method));
    }

    Class<?> eventClass = (Class<?>) typeArguments[0];
    if (!DomainEvent.class.isAssignableFrom(eventClass)) {
      throw new EventuateDomainEventHandlerValidationException(
          String.format("Event handler method %s event type must extend DomainEvent", method));
    }
  }

  private static void validateMethodDeclaringClass(Method method) {
    Class<?> declaringClass = method.getDeclaringClass();
    if (Modifier.isAbstract(declaringClass.getModifiers()) || declaringClass.isInterface()) {
      throw new EventuateDomainEventHandlerValidationException(
          String.format("Event handler method %s must be in a concrete class", method));
    }
  }

  private static void validateAnnotationAttributes(EventuateDomainEventHandler annotation) {
    if (annotation.subscriberId().trim().isEmpty()) {
      throw new EventuateDomainEventHandlerValidationException("subscriberId must not be empty");
    }
    if (annotation.channel().trim().isEmpty()) {
      throw new EventuateDomainEventHandlerValidationException("channel must not be empty");
    }
  }
}