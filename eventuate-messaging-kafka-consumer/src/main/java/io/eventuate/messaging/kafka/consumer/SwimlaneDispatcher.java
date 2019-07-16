package io.eventuate.messaging.kafka.consumer;

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

  private SwimlaneDispatcherBacklog consumerStatus = new SwimlaneDispatcherBacklog(queue);

  public SwimlaneDispatcher(String subscriberId, Integer swimlane, Executor executor) {
    this.subscriberId = subscriberId;
    this.swimlane = swimlane;
    this.executor = executor;
  }

  public boolean getRunning() {
    return running.get();
  }

  public SwimlaneDispatcherBacklog dispatch(KafkaMessage message, Consumer<KafkaMessage> messageConsumer) {
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
    return consumerStatus;
  }

  private void processNextQueuedMessage() {
    executor.execute(this::processQueuedMessage);
  }

  class QueuedMessage {
    KafkaMessage message;
    Consumer<KafkaMessage> messageConsumer;

    public QueuedMessage(KafkaMessage message, Consumer<KafkaMessage> messageConsumer) {
      this.message = message;
      this.messageConsumer = messageConsumer;
    }
  }


  public void processQueuedMessage() {
    while (true) {
      QueuedMessage queuedMessage = getNextMessage();
      if (queuedMessage == null) {
        logger.trace("No queued message for {} {}", subscriberId, swimlane);
        return;
      } else {
        logger.trace("Invoking handler for message for {} {} {}", subscriberId, swimlane, queuedMessage.message);
        try {
          queuedMessage.messageConsumer.accept(queuedMessage.message);
        } catch (RuntimeException e) {
          logger.error("Exception handling message - terminating", e);
          return;
        }
      }
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
