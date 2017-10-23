package io.eventuate.tram.commands.common.paths;

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

public class ResourcePathPattern {
  private final String[] splits;

  public ResourcePathPattern(String pathPattern) {
    Assert.isTrue( pathPattern.startsWith("/"), "Should start with / " + pathPattern );
    this.splits = splitPath(pathPattern);

  }

  private static String[] splitPath(String path) {
    return path.split("/");
  }

  public static ResourcePathPattern parse(String pathPattern) {
    return new ResourcePathPattern(pathPattern);
  }

  public int length() {
    return splits.length;
  }

  public boolean isSatisfiedBy(ResourcePath mr) {
    if (splits.length != mr.splits.length)
      return false;
    for (int i = 0 ; i < mr.splits.length ; i++)
      if (!pathSegmentMatches(splits[i], mr.splits[i]))
        return false;
    return true;
  }

  private boolean pathSegmentMatches(String patternSegment, String pathSegment) {
    return isPlaceholder(patternSegment) || patternSegment.equals(pathSegment);
  }

  private boolean isPlaceholder(String patternSegment) {
    return patternSegment.startsWith("{");
  }

  public Map<String, String> getPathVariableValues(ResourcePath mr) {
    Map<String, String> result = new HashMap<>();
    for (int i = 0 ; i < mr.splits.length ; i++) {
      String name = splits[i];
      if (isPlaceholder(name)) {
         result.put(placeholderName(name), mr.splits[i]);
      }
    }
    return result;
  }

  private String placeholderName(String name) {
    return name.substring(1, name.length() - 1);
  }

  public ResourcePath replacePlaceholders(PlaceholderValueProvider placeholderValueProvider) {
    return new ResourcePath(Arrays.stream(splits).map(s -> isPlaceholder(s) ? placeholderValueProvider.get(placeholderName(s)).orElseGet(() -> {
      throw new RuntimeException("Placeholder not found: " + placeholderName(s) + " in " + s + ", params=" + placeholderValueProvider.getParams());
    }) : s).collect(toList()).toArray(new String[splits.length]));
  }

  public ResourcePath replacePlaceholders(Object[] pathParams) {
    AtomicInteger idx = new AtomicInteger(0);
    return new ResourcePath(Arrays.stream(splits).map(s -> isPlaceholder(s) ? getPlaceholderValue(pathParams, idx.getAndIncrement()).orElseGet(() -> {
      throw new RuntimeException("Placeholder not found: " + placeholderName(s) + " in " + s + ", params=" + Arrays.asList(pathParams));
    }) : s).collect(toList()).toArray(new String[splits.length]));
  }

  private Optional<String> getPlaceholderValue(Object[] pathParams, int idx) {
    return idx < pathParams.length ? Optional.of(pathParams[idx].toString()) : Optional.empty();
  }

}
