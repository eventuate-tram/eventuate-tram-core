package io.eventuate.tram.commands.consumer;

import java.util.Map;

public class PathVariables {

  private Map<String, String> pathVars;

  public PathVariables(Map<String, String> pathVars) {

    this.pathVars = pathVars;
  }

  public String getString(String name) {
    return pathVars.get(name);
  }

  public long getLong(String name) {
    return Long.parseLong(getString(name));
  }

}
