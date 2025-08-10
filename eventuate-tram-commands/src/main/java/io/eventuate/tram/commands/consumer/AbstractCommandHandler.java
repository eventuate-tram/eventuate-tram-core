package io.eventuate.tram.commands.consumer;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.commands.common.paths.ResourcePath;
import io.eventuate.tram.commands.common.paths.ResourcePathPattern;
import io.eventuate.tram.messaging.common.Message;

import java.util.Optional;
import java.util.function.Function;


public class AbstractCommandHandler<RESULT> {

  private final String channel;
  private final Optional<String> resource;
  private final Class commandClass;
  private final Function<CommandHandlerArgs<Command>, RESULT> handler;

  public <C extends Command> AbstractCommandHandler(String channel, Optional<String> resource,
                                    Class<C> commandClass,
                                    Function<CommandHandlerArgs<C>, RESULT> handler) {
    this.channel = channel;
    this.resource = resource;
    this.commandClass = commandClass;
    this.handler = (CommandHandlerArgs<Command> args) -> handler.apply((CommandHandlerArgs<C>)args);
  }

  public String getChannel() {
    return channel;
  }

  public boolean handles(Message message) {
    return commandTypeMatches(message) && resourceMatches(message);
  }

  private boolean resourceMatches(Message message) {
    return resource.isEmpty() || message.getHeader(CommandMessageHeaders.RESOURCE).map(m -> resourceMatches(m, resource.get())).orElse(false);
  }

  private boolean commandTypeMatches(Message message) {
    return commandClass.getName().equals(
            message.getRequiredHeader(CommandMessageHeaders.COMMAND_TYPE));
  }

  private boolean resourceMatches(String messageResource, String methodPath) {
    ResourcePathPattern r = ResourcePathPattern.parse(methodPath);
    ResourcePath mr = ResourcePath.parse(messageResource);
    return r.isSatisfiedBy(mr);
  }

  public Class getCommandClass() {
    return commandClass;
  }

  public Optional<String> getResource() {
    return resource;
  }

  public RESULT invokeMethod(CommandHandlerArgs commandHandlerArgs) {
    return (RESULT) handler.apply(commandHandlerArgs);
  }
}
