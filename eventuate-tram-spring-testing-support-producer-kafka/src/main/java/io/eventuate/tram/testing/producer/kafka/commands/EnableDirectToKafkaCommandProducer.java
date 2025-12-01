package io.eventuate.tram.testing.producer.kafka.commands;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(DirectToKafkaCommandProducerConfiguration.class)
public @interface EnableDirectToKafkaCommandProducer {
}
