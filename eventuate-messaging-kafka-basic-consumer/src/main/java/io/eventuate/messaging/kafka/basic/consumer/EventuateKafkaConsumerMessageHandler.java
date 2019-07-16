package io.eventuate.messaging.kafka.basic.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public interface EventuateKafkaConsumerMessageHandler
        extends BiFunction<ConsumerRecord<String, String>, BiConsumer<Void, Throwable>, MessageConsumerBacklog> {
}
