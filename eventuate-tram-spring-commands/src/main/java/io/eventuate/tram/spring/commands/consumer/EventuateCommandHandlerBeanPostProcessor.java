package io.eventuate.tram.spring.commands.consumer;

import io.eventuate.tram.commands.consumer.annotations.EventuateCommandHandler;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;

import java.lang.reflect.Method;
import java.util.Map;

public class EventuateCommandHandlerBeanPostProcessor implements BeanPostProcessor {

  private final EventuateCommandDispatcher eventuateCommandDispatcher;

  public EventuateCommandHandlerBeanPostProcessor(EventuateCommandDispatcher eventuateCommandDispatcher) {
    this.eventuateCommandDispatcher = eventuateCommandDispatcher;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    Class<?> targetClass = AopUtils.getTargetClass(bean);
    Map<Method, EventuateCommandHandler> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
        (MethodIntrospector.MetadataLookup<EventuateCommandHandler>) method ->
            method.getAnnotation(EventuateCommandHandler.class));
    annotatedMethods.forEach((method, eventuateCommandHandler)
        -> eventuateCommandDispatcher.registerHandlerMethod(bean, eventuateCommandHandler, method));
    return bean;
  }
}
