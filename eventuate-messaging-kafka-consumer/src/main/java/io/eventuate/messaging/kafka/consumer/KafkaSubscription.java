package io.eventuate.messaging.kafka.consumer;

public class KafkaSubscription {
  private Runnable closingCallback;

  public KafkaSubscription(Runnable closingCallback) {
    this.closingCallback = closingCallback;
  }

  public void close() {
    closingCallback.run();
  }
}
