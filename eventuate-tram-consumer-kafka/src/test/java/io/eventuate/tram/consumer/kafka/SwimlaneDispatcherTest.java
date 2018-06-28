package io.eventuate.tram.consumer.kafka;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.util.test.async.Eventually;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class SwimlaneDispatcherTest {

  @Test
  public void testProcessing() {
    SwimlaneDispatcher swimlaneDispatcher = new SwimlaneDispatcher("1", 1, Executors.newCachedThreadPool());

    AtomicInteger counter = new AtomicInteger(0);

    Consumer<Message> handler = msg -> {
      counter.incrementAndGet();
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    };

    //test case when events supplied continuously, and processing is not stopping
    for (int i = 0; i < 5; i++) {
      if (i > 0) {
        Assert.assertTrue(swimlaneDispatcher.getRunning());
      }
      swimlaneDispatcher.dispatch(null, handler);
    }

    Eventually.eventually(() -> {
      Assert.assertEquals(5, counter.get());
      Assert.assertFalse(swimlaneDispatcher.getRunning());
    });


    //test event processing after stop
    for (int i = 0; i < 5; i++) {
      if (i > 0) {
        Assert.assertTrue(swimlaneDispatcher.getRunning());
      }
      swimlaneDispatcher.dispatch(null, handler);
    }

    Eventually.eventually(() -> {
      Assert.assertEquals(10, counter.get());
      Assert.assertFalse(swimlaneDispatcher.getRunning());
    });
  }
}
