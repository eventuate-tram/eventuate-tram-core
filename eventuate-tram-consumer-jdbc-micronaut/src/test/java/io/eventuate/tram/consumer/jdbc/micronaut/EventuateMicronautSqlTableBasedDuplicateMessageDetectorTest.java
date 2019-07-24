package io.eventuate.tram.consumer.jdbc.micronaut;

import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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