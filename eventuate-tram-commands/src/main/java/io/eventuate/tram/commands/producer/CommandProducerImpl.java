package io.eventuate.tram.commands.producer;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageProducer;

import java.util.Map;

import static io.eventuate.tram.commands.producer.CommandMessageFactory.makeMessage;

public class CommandProducerImpl implements CommandProducer {

  private final MessageProducer messageProducer;
  private final CommandNameMapping commandNameMapping;

  public CommandProducerImpl(MessageProducer messageProducer, CommandNameMapping commandNameMapping) {
    this.messageProducer = messageProducer;
    this.commandNameMapping = commandNameMapping;
  }

  @Override
  public String send(String channel, Command command, String replyTo, Map<String, String> headers) {
    return send(channel, null, command, replyTo, headers);
  }

  @Override
  public String sendNotification(String channel, Command command, Map<String, String> headers) {
    return send(channel, null, command, null, headers);
  }

  @Override
  public String send(String channel, String resource, Command command, String replyTo, Map<String, String> headers) {
    Message message = makeMessage(commandNameMapping, channel, resource, command, replyTo, headers);
    messageProducer.send(channel, message);
    return message.getId();
  }
}
