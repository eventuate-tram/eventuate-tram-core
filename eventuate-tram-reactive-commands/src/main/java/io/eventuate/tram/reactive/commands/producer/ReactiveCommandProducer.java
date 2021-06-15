package io.eventuate.tram.reactive.commands.producer;

import io.eventuate.tram.commands.common.Command;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface ReactiveCommandProducer {
  Mono<String> send(String channel, Command command, String replyTo, Map<String, String> headers);
  Mono<String> send(String channel, String resource, Command command, String replyTo, Map<String, String> headers);
}
