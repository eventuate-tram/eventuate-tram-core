package io.eventuate.tram.spring.commands.consumer;

import io.eventuate.tram.commands.consumer.annotations.EventuateCommandHandler;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventuateCommandHandlerBeanPostProcessor implements BeanPostProcessor {

  private final EventuateCommandDispatcher eventuateCommandDispatcher;

  public EventuateCommandHandlerBeanPostProcessor(EventuateCommandDispatcher eventuateCommandDispatcher) {
    this.eventuateCommandDispatcher = eventuateCommandDispatcher;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    List<CommandHandlerInfo> commandHandlerInfos = commandHandlersFromBean(bean);
    commandHandlerInfos.forEach(eventuateCommandDispatcher::registerHandlerMethod);
    return bean;
  }

  public static List<CommandHandlerInfo> commandHandlersFromBean(Object bean) {
    Class<?> targetClass = AopUtils.getTargetClass(bean);
    Map<Method, EventuateCommandHandler> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
        (MethodIntrospector.MetadataLookup<EventuateCommandHandler>) method ->
            method.getAnnotation(EventuateCommandHandler.class));
    return annotatedMethods.entrySet().stream()
        .map( entry -> new CommandHandlerInfo(bean, entry.getValue(), entry.getKey()))
        .collect(Collectors.toList());
  }
}
