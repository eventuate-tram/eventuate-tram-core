package io.eventuate.tram.consumer.kafka;

import io.eventuate.tram.messaging.common.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SwimlaneDispatcher {

  private static Logger logger = LoggerFactory.getLogger(SwimlaneDispatcher.class);

  private String subscriberId;
  private Integer swimlane;
  private Executor executor;

  private final LinkedBlockingQueue<QueuedMessage> queue = new LinkedBlockingQueue<>();
  private AtomicBoolean running = new AtomicBoolean(false);

  public SwimlaneDispatcher(String subscriberId, Integer swimlane, Executor executor) {
    this.subscriberId = subscriberId;
    this.swimlane = swimlane;
    this.executor = executor;
  }

  public boolean getRunning() {
    return running.get();
  }

  public void dispatch(Message message, Consumer<Message> messageConsumer) {
    synchronized (queue) {
      QueuedMessage queuedMessage = new QueuedMessage(message, messageConsumer);
      queue.add(queuedMessage);
      logger.trace("added message to queue: {} {} {}", subscriberId, swimlane, message);
      if (running.compareAndSet(false, true)) {
        logger.trace("Stopped - attempting to process newly queued message: {} {}", subscriberId, swimlane);
        processNextQueuedMessage();
      } else
        logger.trace("Running - Not attempting to process newly queued message: {} {}", subscriberId, swimlane);
    }
  }

  private void processNextQueuedMessage() {
    executor.execute(this::processQueuedMessage);
  }

  class QueuedMessage {
    Message message;
    Consumer<Message> messageConsumer;

    public QueuedMessage(Message message, Consumer<Message> messageConsumer) {
      this.message = message;
      this.messageConsumer = messageConsumer;
    }
  }


  public void processQueuedMessage() {
    QueuedMessage queuedMessage = getNextMessage();
    if (queuedMessage == null)
      logger.trace("No queued message for {} {}", subscriberId, swimlane);
    else {
      logger.trace("Invoking handler for message for {} {} {}", subscriberId, swimlane, queuedMessage.message);
      queuedMessage.messageConsumer.accept(queuedMessage.message);
      processNextQueuedMessage();
    }
  }

  private QueuedMessage getNextMessage() {
    QueuedMessage queuedMessage = queue.poll();
    if (queuedMessage != null)
      return queuedMessage;

    synchronized (queue) {
      queuedMessage = queue.poll();
      if (queuedMessage == null) {
        running.compareAndSet(true, false);
      }
      return queuedMessage;
    }
  }
}
