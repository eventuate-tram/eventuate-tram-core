package io.eventuate.tram.reactive.commands.producer;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import reactor.core.publisher.Mono;

import java.util.Map;

import static io.eventuate.tram.commands.producer.CommandMessageFactory.makeMessage;

public class ReactiveCommandProducerImpl implements ReactiveCommandProducer {

  private final ReactiveMessageProducer messageProducer;
  private final CommandNameMapping commandNameMapping;

  public ReactiveCommandProducerImpl(ReactiveMessageProducer messageProducer, CommandNameMapping commandNameMapping) {
    this.messageProducer = messageProducer;
    this.commandNameMapping = commandNameMapping;
  }

  @Override
  public Mono<String> send(String channel, Command command, String replyTo, Map<String, String> headers) {
    return send(channel, null, command, replyTo, headers);
  }

  @Override
  public Mono<String> sendNotification(String channel, Command command, Map<String, String> headers) {
    return send(channel, null, command, null, headers);
  }

  @Override
  public Mono<String> send(String channel, String resource, Command command, String replyTo, Map<String, String> headers) {
    Message message = makeMessage(commandNameMapping, channel, resource, command, replyTo, headers);

    return messageProducer.send(channel, message).map(Message::getId);
  }
}
