package io.eventuate.tram.consumer.common.reactive;


import io.eventuate.tram.messaging.common.Message;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface ReactiveMessageHandler extends Function<Message, Mono<Void>> {
}
