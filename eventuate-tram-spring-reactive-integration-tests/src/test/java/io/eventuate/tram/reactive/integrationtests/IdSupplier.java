package io.eventuate.tram.reactive.integrationtests;

import java.util.UUID;

public class IdSupplier {
  public static String get() {
    return UUID.randomUUID().toString();
  }
}
