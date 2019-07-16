package io.eventuate.messaging.kafka.consumer;

import java.util.function.Consumer;

public interface KafkaMessageHandler extends Consumer<KafkaMessage> {

}
