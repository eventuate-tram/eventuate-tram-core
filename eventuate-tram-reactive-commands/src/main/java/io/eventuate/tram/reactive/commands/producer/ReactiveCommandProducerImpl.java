package io.eventuate.tram.reactive.commands.producer;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import reactor.core.publisher.Mono;

import java.util.Map;

import static io.eventuate.tram.commands.producer.CommandMessageFactory.makeMessage;

public class ReactiveCommandProducerImpl implements ReactiveCommandProducer {

  private ReactiveMessageProducer messageProducer;

  public ReactiveCommandProducerImpl(ReactiveMessageProducer messageProducer) {
    this.messageProducer = messageProducer;
  }

  @Override
  public Mono<String> send(String channel, Command command, String replyTo, Map<String, String> headers) {
    return send(channel, null, command, replyTo, headers);
  }

  @Override
  public Mono<String> send(String channel, String resource, Command command, String replyTo, Map<String, String> headers) {
    Message message = makeMessage(channel, resource, command, replyTo, headers);

    return messageProducer.send(channel, message).map(Message::getId);
  }
}
