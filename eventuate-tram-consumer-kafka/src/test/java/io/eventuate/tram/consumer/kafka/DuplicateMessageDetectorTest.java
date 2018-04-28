package io.eventuate.tram.consumer.kafka;

import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DuplicateMessageDetectorTestConfiguration.class)
public class DuplicateMessageDetectorTest {

  @Autowired
  private DuplicateMessageDetector duplicateMessageDetector;

  @Test
  public void shouldDetectDuplicate() {

    String consumerId = getClass().getName();
    String messageId = Long.toString(System.currentTimeMillis());

    assertFalse(duplicateMessageDetector.isDuplicate(consumerId, messageId));
    assertTrue(duplicateMessageDetector.isDuplicate(consumerId, messageId));
  }

}