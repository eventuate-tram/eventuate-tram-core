package io.eventuate.tram.spring.events.subscriber;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.annotations.EventuateDomainEventHandler;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventuateDomainEventHandlerBeanPostProcessor implements BeanPostProcessor {

  private final EventuateDomainEventDispatcher eventuateDomainEventDispatcher;

  public EventuateDomainEventHandlerBeanPostProcessor(EventuateDomainEventDispatcher eventuateDomainEventDispatcher) {
    this.eventuateDomainEventDispatcher = eventuateDomainEventDispatcher;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    List<EventuateDomainEventHandlerInfo> commandHandlerInfos = eventHandlersFromBean(bean);
    commandHandlerInfos.forEach(eventuateDomainEventDispatcher::registerHandlerMethod);
    return bean;
  }

  public static List<EventuateDomainEventHandlerInfo> eventHandlersFromBean(Object bean) {
    Class<?> targetClass = AopUtils.getTargetClass(bean);
    Map<Method, EventuateDomainEventHandler> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
        (MethodIntrospector.MetadataLookup<EventuateDomainEventHandler>) method ->
            method.getAnnotation(EventuateDomainEventHandler.class));
    return annotatedMethods.entrySet().stream()
        .map(entry -> {
          EventuateDomainEventHandler annotation = entry.getValue();
          return EventuateDomainEventHandlerInfo.make(bean, annotation.subscriberId(), annotation.channel(), entry.getKey());
        })
        .collect(Collectors.toList());
  }

}
