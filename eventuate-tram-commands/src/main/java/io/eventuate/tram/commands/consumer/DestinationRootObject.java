package io.eventuate.tram.commands.consumer;

import java.util.Map;

public class DestinationRootObject {
  private final Object parameter;
  private final Object result;
  private Map<String, String> path;

  public DestinationRootObject(Object parameter, Object result, Map<String, String> path) {
    this.parameter = parameter;
    this.result = result;
    this.path = path;
  }

  public Object getParameter() {
    return parameter;
  }

  public Object getResult() {
    return result;
  }

  public Map<String, String> getPath() {
    return path;
  }
}
