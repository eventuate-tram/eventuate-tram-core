package io.eventuate.tram.commands.common.paths;

import org.springframework.util.Assert;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;

public class ResourcePath {

  final String[] splits;

  public ResourcePath(String[] splits) {
    this.splits = splits;
  }

  public ResourcePath(String resource) {
    Assert.isTrue( resource.startsWith("/"), "Should start with / " + resource );
    this.splits = splitPath(resource);
  }

  private String[] splitPath(String path) {
    return path.split("/");
  }

  public static ResourcePath parse(String resource) {
    return new ResourcePath(resource);
  }

  public int length() {
    return splits.length;
  }

  public String toPath() {
    return Arrays.stream(splits).collect(joining("/"));
  }
}
