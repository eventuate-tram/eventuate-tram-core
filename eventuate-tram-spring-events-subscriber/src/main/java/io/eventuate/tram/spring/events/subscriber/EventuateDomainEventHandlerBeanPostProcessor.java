package io.eventuate.tram.spring.events.subscriber;

import io.eventuate.tram.events.subscriber.annotations.EventuateDomainEventHandler;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;

import java.lang.reflect.Method;
import java.util.Map;

public class EventuateDomainEventHandlerBeanPostProcessor implements BeanPostProcessor {

  private final EventuateDomainEventDispatcher eventuateDomainEventDispatcher;

  public EventuateDomainEventHandlerBeanPostProcessor(EventuateDomainEventDispatcher eventuateDomainEventDispatcher) {
    this.eventuateDomainEventDispatcher = eventuateDomainEventDispatcher;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    Class<?> targetClass = AopUtils.getTargetClass(bean);
    Map<Method, EventuateDomainEventHandler> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
        (MethodIntrospector.MetadataLookup<EventuateDomainEventHandler>) method ->
            method.getAnnotation(EventuateDomainEventHandler.class));

    annotatedMethods.forEach((method, eventuateDomainEventHandler) -> {
      EventuateDomainEventHandlerMethodValidator.validateEventHandlerMethod(method, eventuateDomainEventHandler);
      eventuateDomainEventDispatcher.registerHandlerMethod(bean, eventuateDomainEventHandler, method);
    });

    return bean;
  }
}