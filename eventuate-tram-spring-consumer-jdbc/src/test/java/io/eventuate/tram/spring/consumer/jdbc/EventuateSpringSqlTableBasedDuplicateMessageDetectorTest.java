package io.eventuate.tram.spring.consumer.jdbc;

import io.eventuate.common.spring.jdbc.EventuateCommonJdbcOperationsConfiguration;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {EventuateSpringSqlTableBasedDuplicateMessageDetectorTest.DuplicateMessageDetectorTestConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class EventuateSpringSqlTableBasedDuplicateMessageDetectorTest {

  @Autowired
  private DuplicateMessageDetector duplicateMessageDetector;

  @Configuration
  @EnableAutoConfiguration
  @Import({EventuateCommonJdbcOperationsConfiguration.class})
  static public class DuplicateMessageDetectorTestConfiguration {
  }

  @Test
  public void shouldDetectDuplicate() {

    String consumerId = getClass().getName();
    String messageId = Long.toString(System.currentTimeMillis());

    assertFalse(duplicateMessageDetector.isDuplicate(consumerId, messageId));
    assertTrue(duplicateMessageDetector.isDuplicate(consumerId, messageId));
  }

}