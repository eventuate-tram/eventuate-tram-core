package io.eventuate.tram.micronaut.commands.common;

import io.eventuate.tram.commands.common.CommandNameMapping;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

@MicronautTest
public class TramCommandCommonFactoryTest {

  @Inject
  CommandNameMapping commandNameMapping;

  @Test
  public void shouldLoadCommandNamingBean() {
    Assertions.assertNotNull(commandNameMapping);
  }
}
