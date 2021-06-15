package io.eventuate.tram.commands.consumer;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.commands.common.paths.ResourcePath;
import io.eventuate.tram.commands.common.paths.ResourcePathPattern;
import io.eventuate.tram.messaging.common.Message;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.EMPTY_MAP;

public class CommandHandlerParams {
  private Object command;
  private Map<String, String> correlationHeaders;
  private Map<String, String> pathVars;
  private Optional<String> defaultReplyChannel;

  public CommandHandlerParams(Message message, Class<?> commandClass, Optional<String> resource) {
    command = JSonMapper.fromJson(message.getPayload(), commandClass);
    pathVars = getPathVars(message, resource);
    correlationHeaders = getCorrelationHeaders(message.getHeaders());
    defaultReplyChannel = message.getHeader(CommandMessageHeaders.REPLY_TO);
  }

  public Object getCommand() {
    return command;
  }

  public Map<String, String> getCorrelationHeaders() {
    return correlationHeaders;
  }

  public Map<String, String> getPathVars() {
    return pathVars;
  }

  public Optional<String> getDefaultReplyChannel() {
    return defaultReplyChannel;
  }

  private Map<String, String> getCorrelationHeaders(Map<String, String> headers) {
    Map<String, String> m = headers.entrySet()
            .stream()
            .filter(e -> e.getKey().startsWith(CommandMessageHeaders.COMMAND_HEADER_PREFIX))
            .collect(Collectors.toMap(e -> CommandMessageHeaders.inReply(e.getKey()),
                    Map.Entry::getValue));
    m.put(ReplyMessageHeaders.IN_REPLY_TO, headers.get(Message.ID));
    return m;
  }

  private Map<String, String> getPathVars(Message message, Optional<String> handlerResource) {
    return handlerResource.flatMap( res -> {
      ResourcePathPattern r = ResourcePathPattern.parse(res);
      return message.getHeader(CommandMessageHeaders.RESOURCE).map(h -> {
        ResourcePath mr = ResourcePath.parse(h);
        return r.getPathVariableValues(mr);
      });
    }).orElse(EMPTY_MAP);
  }
}
