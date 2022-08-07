package io.eventuate.tram.commands.producer;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;

import java.util.Map;

public class CommandMessageFactory {
    public static Message makeMessage(CommandNameMapping commandNameMapping, String channel, Command command, String replyTo, Map<String, String> headers) {
        return makeMessage(commandNameMapping, channel, null, command, replyTo, headers);
    }

    public static Message makeMessage(CommandNameMapping commandNameMapping, String channel, String resource, Command command, String replyTo, Map<String, String> headers) {
        MessageBuilder builder = MessageBuilder.withPayload(JSonMapper.toJson(command))
                .withExtraHeaders("", headers) // TODO should these be prefixed??!
                .withHeader(CommandMessageHeaders.DESTINATION, channel)
                .withHeader(CommandMessageHeaders.COMMAND_TYPE, commandNameMapping.commandToExternalCommandType(command))
                ;

        if (replyTo != null)
            builder.withHeader(CommandMessageHeaders.REPLY_TO, replyTo);

        if (resource != null)
            builder.withHeader(CommandMessageHeaders.RESOURCE, resource);

        return builder.build();
    }
}
