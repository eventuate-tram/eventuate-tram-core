package io.eventuate.messaging.kafka.basic.consumer;

public enum EventuateKafkaConsumerState {
  MESSAGE_HANDLING_FAILED, STARTED, FAILED_TO_START, STOPPED, FAILED, CREATED
}
