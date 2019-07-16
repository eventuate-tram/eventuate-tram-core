package io.eventuate.messaging.kafka.consumer;

import io.eventuate.messaging.kafka.basic.consumer.MessageConsumerBacklog;

import java.util.concurrent.LinkedBlockingQueue;

public class SwimlaneDispatcherBacklog implements MessageConsumerBacklog {
  private final LinkedBlockingQueue<?> queue;

  public SwimlaneDispatcherBacklog(LinkedBlockingQueue<?> queue) {
    this.queue = queue;
  }

  @Override
  public int size() {
    return queue.size();
  }
}
