package io.eventuate.tram.commands.common.paths;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class SingleValuePlaceholderValueProvider implements PlaceholderValueProvider {
  private Object pathParam;
  private boolean used;

  public SingleValuePlaceholderValueProvider(Object pathParam) {
    this.pathParam = pathParam;
  }

  @Override
  public Optional<String> get(String name) {
    if (!used) {
      used = true;
      return Optional.of(pathParam.toString());
    } else
      return Optional.empty();
  }

  @Override
  public Map<String, String> getParams() {
    return Collections.singletonMap("singleValue", pathParam.toString());
  }
}
