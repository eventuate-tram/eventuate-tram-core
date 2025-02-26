package io.eventuate.tram.spring.commands.consumer;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.annotations.EventuateCommandHandler;
import io.eventuate.tram.common.TypeParameterExtractor;

import java.lang.reflect.Method;

public final class CommandHandlerInfo {
  private final Object target;
  private final EventuateCommandHandler eventuateCommandHandler;
  private final Method method;

  public CommandHandlerInfo(Object target, EventuateCommandHandler eventuateCommandHandler, Method method) {
    this.target = target;
    this.eventuateCommandHandler = eventuateCommandHandler;
    this.method = method;
  }

  public Object getTarget() {
    return target;
  }

  public EventuateCommandHandler getEventuateCommandHandler() {
    return eventuateCommandHandler;
  }

  public Method getMethod() {
    return method;
  }

  public String getChannel() {
    return eventuateCommandHandler.channel();
  }

  public Class<? extends Command> getCommandClass() {
    return (Class<? extends Command>) TypeParameterExtractor.extractTypeParameter(method);
  }

  public Class<?> getTargetClass() {
    return target.getClass();
  }

  public Class<?> getReturnType() {
    return method.getReturnType();
  }

}
