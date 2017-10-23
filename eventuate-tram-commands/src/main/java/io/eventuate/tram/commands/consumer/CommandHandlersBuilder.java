package io.eventuate.tram.commands.consumer;


import io.eventuate.tram.messaging.common.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class CommandHandlersBuilder {
  private String channel;
  private Optional<String> resource = Optional.empty();

  private List<CommandHandler> handlers = new ArrayList<>();

  public static CommandHandlersBuilder fromChannel(String channel) {
    return new CommandHandlersBuilder().andFromChannel(channel);
  }

  private CommandHandlersBuilder andFromChannel(String channel) {
    this.channel = channel;
    return this;
  }

  public CommandHandlersBuilder resource(String resource) {
    this.resource = Optional.of(resource);
    return this;
  }

  public <C> CommandHandlersBuilder onMessageReturningMessages(Class<C> commandClass,
                                                               BiFunction<CommandMessage<C>, PathVariables, List<Message>> handler) {
    this.handlers.add(new CommandHandler(channel, resource, commandClass, handler));
    return this;
  }

  public <C> CommandHandlersBuilder onMessageReturningOptionalMessage(Class<C> commandClass,
                                                                      BiFunction<CommandMessage<C>, PathVariables, Optional<Message>> handler) {
    this.handlers.add(new CommandHandler(channel, resource, commandClass,
            (c, pv) -> handler.apply(c, pv).map(Collections::singletonList).orElse(Collections.EMPTY_LIST)));
    return this;
  }

  public <C> CommandHandlersBuilder onMessage(Class<C> commandClass,
                                              BiFunction<CommandMessage<C>, PathVariables, Message> handler) {
    this.handlers.add(new CommandHandler(channel, resource, commandClass, (c, pv) -> Collections.singletonList(handler.apply(c, pv))));
    return this;
  }

  public CommandHandlers build() {
    return new CommandHandlers(handlers);
  }
}
