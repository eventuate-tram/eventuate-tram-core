package io.eventuate.tram.commands.common.paths;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResourcePathPatternTest {

  @Test
  public void shouldReplacePlaceholders() {
    ResourcePathPattern rpp = new ResourcePathPattern("/foo/{bar}");
    ResourcePath rp = rpp.replacePlaceholders(new SingleValuePlaceholderValueProvider("baz"));

    assertEquals("/foo/baz", rp.toPath());
  }

}