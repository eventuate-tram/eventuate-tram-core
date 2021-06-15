package io.eventuate.tram.commands.producer;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageProducer;

import java.util.Map;

import static io.eventuate.tram.commands.producer.CommandMessageFactory.makeMessage;

public class CommandProducerImpl implements CommandProducer {

  private MessageProducer messageProducer;

  public CommandProducerImpl(MessageProducer messageProducer) {
    this.messageProducer = messageProducer;
  }

  @Override
  public String send(String channel, Command command, String replyTo, Map<String, String> headers) {
    return send(channel, null, command, replyTo, headers);
  }

  @Override
  public String send(String channel, String resource, Command command, String replyTo, Map<String, String> headers) {
    Message message = makeMessage(channel, resource, command, replyTo, headers);
    messageProducer.send(channel, message);
    return message.getId();
  }
}
