package io.eventuate.tram.commands.consumer;

import java.util.Map;

public class DestinationRootObjectForException extends DestinationRootObject {
  private final Throwable throwable;

  public Throwable getThrowable() {
    return throwable;
  }

  public DestinationRootObjectForException(Object parameter, Object result, Map<String, String> pathVars, Throwable throwable) {
    super(parameter, result, pathVars);
    this.throwable = throwable;

  }
}
