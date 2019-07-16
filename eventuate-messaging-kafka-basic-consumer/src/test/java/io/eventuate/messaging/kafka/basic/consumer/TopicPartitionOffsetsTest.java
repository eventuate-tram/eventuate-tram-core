package io.eventuate.messaging.kafka.basic.consumer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TopicPartitionOffsetsTest  {

  @Test
  public void shouldTrackOffsets() {

    TopicPartitionOffsets tpo = new TopicPartitionOffsets();

    tpo.noteUnprocessed(1);
    tpo.noteUnprocessed(2);
    tpo.noteUnprocessed(3);

    tpo.noteProcessed(2);

    assertFalse(tpo.offsetToCommit().isPresent());

    tpo.noteProcessed(1);

    assertEquals(new Long(2), tpo.offsetToCommit().get());

    tpo.noteProcessed(3);

    assertEquals(new Long(3), tpo.offsetToCommit().get());
  }


}
