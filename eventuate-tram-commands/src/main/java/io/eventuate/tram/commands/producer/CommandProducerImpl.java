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
  public String send(String channel, String resource, Command command, Map<String, String> headers) {
    Message message = makeMessage(resource, command, headers);
    messageProducer.send(channelMapping.transform(channel), message);
    return message.getId();
  }

  public static Message makeMessage(String resource, Command command, Map<String, String> headers) {
    return MessageBuilder.withPayload(JSonMapper.toJson(command))
              .withExtraHeaders("", headers) // TODO should these be prefixed??!
              .withHeader(CommandMessageHeaders.RESOURCE, resource)
              .withHeader(CommandMessageHeaders.COMMAND_TYPE, command.getClass().getName()
              )
              .build();
  }
}
