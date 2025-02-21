package io.eventuate.tram.spring.events.subscriber;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.*;
import io.eventuate.tram.events.subscriber.annotations.EventuateDomainEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class EventuateDomainEventDispatcher implements SmartLifecycle {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final DomainEventDispatcherFactory domainEventDispatcherFactory;
  private final List<EventuateDomainEventHandlerInfo> eventHandlers = new ArrayList<>();
  private boolean running = false;
  private List<DomainEventDispatcher> dispatchers;

  public EventuateDomainEventDispatcher(DomainEventDispatcherFactory domainEventDispatcherFactory) {
    this.domainEventDispatcherFactory = domainEventDispatcherFactory;
  }

  public void registerHandlerMethod(Object bean, EventuateDomainEventHandler eventuateDomainEventHandler, Method method) {
    logger.info("Registering event handler method: {}", method);
    EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(method, eventuateDomainEventHandler);
    eventHandlers.add(new EventuateDomainEventHandlerInfo(bean, eventuateDomainEventHandler, method));
  }

  @Override
  public void start() {
    logger.info("Starting EventuateDomainEventDispatcher");
    Map<String, List<EventuateDomainEventHandlerInfo>> groupedEventHandlers = eventHandlers.stream()
        .collect(Collectors.groupingBy(eh -> eh.getEventuateDomainEventHandler().subscriberId()));

    this.dispatchers = groupedEventHandlers.entrySet()
        .stream()
        .map(e -> domainEventDispatcherFactory
            .make(e.getKey(), makeDomainEventHandlers(e.getValue())))
        .collect(Collectors.toList());

    logger.info("Started EventuateDomainEventDispatcher {}", dispatchers);
    running = true;
  }

  private static DomainEventHandlers makeDomainEventHandlers(List<EventuateDomainEventHandlerInfo> eventHandlers) {
    Map<String, List<EventuateDomainEventHandlerInfo>> groupedByChannel = eventHandlers.stream()
        .collect(Collectors.groupingBy(eh -> eh.getEventuateDomainEventHandler().channel()));

    AtomicReference<DomainEventHandlersBuilder> builder = new AtomicReference<>();
    groupedByChannel.forEach((channel, handlers) -> {
      handlers.forEach(eh -> {
        Class<? extends DomainEvent> eventClass = extractEventClass(eh.getMethod());
        if (builder.get() == null) {
          builder.set(DomainEventHandlersBuilder.forAggregateType(channel));
        } else {
          builder.get().andForAggregateType(channel);
        }
        builder.get().onEvent(eventClass, envelope -> {
          try {
            eh.getMethod().invoke(eh.getTarget(), envelope);
          } catch (Exception e) {
            throw new RuntimeException("Error invoking event handler method", e);
          }
        });
      });
    });
    return builder.get().build();
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends DomainEvent> extractEventClass(Method method) {
    Type[] parameterTypes = method.getGenericParameterTypes();
    if (parameterTypes.length != 1 || !(parameterTypes[0] instanceof ParameterizedType)) {
      throw new RuntimeException("Event handler method must have exactly one parameter of type DomainEventEnvelope<T extends DomainEvent>");
    }

    ParameterizedType parameterType = (ParameterizedType) parameterTypes[0];
    if (!DomainEventEnvelope.class.equals(parameterType.getRawType())) {
      throw new RuntimeException("Event handler method parameter must be of type DomainEventEnvelope<T extends DomainEvent>");
    }

    Type[] typeArguments = parameterType.getActualTypeArguments();
    if (typeArguments.length != 1 || !(typeArguments[0] instanceof Class)) {
      throw new RuntimeException("Invalid event type parameter");
    }

    Class<?> eventClass = (Class<?>) typeArguments[0];
    if (!DomainEvent.class.isAssignableFrom(eventClass)) {
      throw new RuntimeException("Event type must extend DomainEvent");
    }

    return (Class<? extends DomainEvent>) eventClass;
  }

  @Override
  public void stop() {
    running = false;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

}
