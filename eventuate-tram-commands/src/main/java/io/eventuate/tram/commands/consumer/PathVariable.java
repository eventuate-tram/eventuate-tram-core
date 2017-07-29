package io.eventuate.tram.commands.consumer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PathVariable {
  String value();
}
