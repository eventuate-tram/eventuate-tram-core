package io.eventuate.tram.inmemory.micronaut;

public class EmbeddedSchema {
  private String resourcePath;

  public EmbeddedSchema(String resourcePath) {
    this.resourcePath = resourcePath;
  }

  public String getResourcePath() {
    return resourcePath;
  }

  public void setResourcePath(String resourcePath) {
    this.resourcePath = resourcePath;
  }
}
