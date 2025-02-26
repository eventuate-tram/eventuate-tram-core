package io.eventuate.tram.spring.commands.consumer;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.common.TypeParameterExtractor;

import java.lang.reflect.Method;

public final class CommandHandlerInfo {
  private final Object target;
  private final String subscriberId;
  private final String channel;
  private final Method method;

  public CommandHandlerInfo(Object target, String subscriberId, String channel, Method method) {
    this.target = target;
    this.subscriberId = subscriberId;
    this.channel = channel;
    this.method = method;
  }

  public Object getTarget() {
    return target;
  }

  public String getSubscriberId() {
    return subscriberId;
  }

  public String getChannel() {
    return channel;
  }

  public Method getMethod() {
    return method;
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
