package io.eventuate.tram.commands.consumer;

public class DestinationForExceptionRootObject {
  private final Object parameter;
  private final Throwable cause;

  public Object getParameter() {
    return parameter;
  }

  public Throwable getCause() {
    return cause;
  }

  public DestinationForExceptionRootObject(Object parameter, Throwable cause) {
    this.parameter = parameter;
    this.cause = cause;
  }
}
