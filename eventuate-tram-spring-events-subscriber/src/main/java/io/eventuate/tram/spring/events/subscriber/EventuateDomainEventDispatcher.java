package io.eventuate.tram.spring.events.subscriber;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.*;
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

  public void registerHandlerMethod(EventuateDomainEventHandlerInfo eventHandler) {
    logger.info("Registering event handler method: {}", eventHandler);
    EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(eventHandler);
    eventHandlers.add(eventHandler);
  }

  public List<EventuateDomainEventHandlerInfo> getEventHandlers() {
    return eventHandlers;
  }

  @Override
  public void start() {
    logger.info("Starting EventuateDomainEventDispatcher");
    Map<String, List<EventuateDomainEventHandlerInfo>> groupedEventHandlers = eventHandlers.stream()
        .collect(Collectors.groupingBy(EventuateDomainEventHandlerInfo::getSubscriberId));

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
        .collect(Collectors.groupingBy(EventuateDomainEventHandlerInfo::getChannel));

    AtomicReference<DomainEventHandlersBuilder> builder = new AtomicReference<>();
    groupedByChannel.forEach((channel, handlers) -> {
      handlers.forEach(eh -> {
        Class<? extends DomainEvent> eventClass = eh.getEventClass();
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

  @Override
  public void stop() {
    running = false;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

}
