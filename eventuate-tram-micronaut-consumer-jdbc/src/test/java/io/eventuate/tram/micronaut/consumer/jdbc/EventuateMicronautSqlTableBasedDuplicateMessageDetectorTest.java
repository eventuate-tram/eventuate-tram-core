package io.eventuate.tram.micronaut.consumer.jdbc;

import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
public class EventuateMicronautSqlTableBasedDuplicateMessageDetectorTest {

  @Inject
  private DuplicateMessageDetector duplicateMessageDetector;

  @Test
  public void shouldDetectDuplicate() {
    String consumerId = getClass().getName();
    String messageId = Long.toString(System.currentTimeMillis());

    assertFalse(duplicateMessageDetector.isDuplicate(consumerId, messageId));
    assertTrue(duplicateMessageDetector.isDuplicate(consumerId, messageId));
  }

}