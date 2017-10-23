package io.eventuate.tram.commands.producer;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.commands.common.ChannelMapping;
import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;

import java.util.Map;

public class CommandProducerImpl implements CommandProducer {

  private MessageProducer messageProducer;
  private ChannelMapping channelMapping;

  public CommandProducerImpl(MessageProducer messageProducer, ChannelMapping channelMapping) {
    this.messageProducer = messageProducer;
    this.channelMapping = channelMapping;
  }

  @Override
  public String send(String channel, Command command, String replyTo, Map<String, String> headers) {
    return send(channel, null, command, replyTo, headers);
  }

  @Override
  public String send(String channel, String resource, Command command, String replyTo, Map<String, String> headers) {
    Message message = makeMessage(channel, resource, command, replyTo, headers);
    messageProducer.send(channelMapping.transform(channel), message);
    return message.getId();
  }

  public static Message makeMessage(String channel, String resource, Command command, String replyTo, Map<String, String> headers) {
    MessageBuilder builder = MessageBuilder.withPayload(JSonMapper.toJson(command))
            .withExtraHeaders("", headers) // TODO should these be prefixed??!
            .withHeader(CommandMessageHeaders.DESTINATION, channel)
            .withHeader(CommandMessageHeaders.COMMAND_TYPE, command.getClass().getName())
            .withHeader(CommandMessageHeaders.REPLY_TO, replyTo);

    if (resource != null)
      builder.withHeader(CommandMessageHeaders.RESOURCE, resource);

    return builder
            .build();
  }
}
