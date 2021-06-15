package io.eventuate.tram.consumer.common.reactive;


import io.eventuate.tram.messaging.common.Message;
import org.reactivestreams.Publisher;

import java.util.function.Function;

public interface ReactiveMessageHandler extends Function<Message, Publisher<?>> {
}
