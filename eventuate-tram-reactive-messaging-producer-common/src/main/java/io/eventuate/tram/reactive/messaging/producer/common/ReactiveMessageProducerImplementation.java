package io.eventuate.tram.reactive.messaging.producer.common;

import io.eventuate.tram.messaging.common.Message;
import reactor.core.publisher.Mono;

public interface ReactiveMessageProducerImplementation {

  Mono<Message> send(Message message);
}
