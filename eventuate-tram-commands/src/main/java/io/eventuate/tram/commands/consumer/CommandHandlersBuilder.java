package io.eventuate.tram.commands.consumer;


import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.messaging.common.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommandHandlersBuilder {
  private String channel;
  private Optional<String> resource = Optional.empty();

  private final List<CommandHandler> handlers = new ArrayList<>();

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

  public <C extends Command> CommandHandlersBuilder onMessageReturningMessages(Class<C> commandClass,
                                                                               BiFunction<CommandMessage<C>, PathVariables, List<Message>> handler) {
    this.handlers.add(new CommandHandler(channel, resource, commandClass, CommandHandlerArgs.makeFn(handler)));
    return this;
  }

  public <C extends Command> CommandHandlersBuilder onMessageReturningOptionalMessage(Class<C> commandClass,
                                                                                      BiFunction<CommandMessage<C>, PathVariables, Optional<Message>> handler) {
    this.handlers.add(new CommandHandler(channel, resource, commandClass,
            args -> handler.apply(args.getCommandMessage(), args.getPathVars()).map(Collections::singletonList).orElse(Collections.emptyList())));
    return this;
  }

  public <C extends Command> CommandHandlersBuilder onMessage(Class<C> commandClass,
                                                              BiFunction<CommandMessage<C>, PathVariables, Message> handler) {
    this.handlers.add(new CommandHandler(channel, resource, commandClass, args ->  Collections.singletonList(handler.apply(args.getCommandMessage(), args.getPathVars()))));
    return this;
  }

  public <C extends Command> CommandHandlersBuilder onMessageReturningMessages(Class<C> commandClass,
                                                               Function<CommandMessage<C>, List<Message>> handler) {
    this.handlers.add(new CommandHandler(channel, resource, commandClass, args ->  handler.apply(args.getCommandMessage())));
    return this;
  }

  public <C extends Command> CommandHandlersBuilder onMessageReturningOptionalMessage(Class<C> commandClass,
                                                                      Function<CommandMessage<C>, Optional<Message>> handler) {
    this.handlers.add(new CommandHandler(channel, resource, commandClass,
            args ->  handler.apply(args.getCommandMessage()).map(Collections::singletonList).orElse(Collections.emptyList())));
    return this;
  }

  public <C extends Command> CommandHandlersBuilder onMessage(Class<C> commandClass,
                                              Function<CommandMessage<C>, Message> handler) {
    this.handlers.add(new CommandHandler(channel, resource, commandClass, args ->  Collections.singletonList(handler.apply(args.getCommandMessage()))));
    return this;
  }

  public <C extends Command> CommandHandlersBuilder onComplexMessage(Class<C> commandClass,
                                              BiConsumer<CommandMessage<C>, CommandReplyToken> handler) {
    this.handlers.add(new CommandHandler(channel, resource, commandClass, makeFn(handler)));
    return this;
  }

  private <C extends Command> Function<CommandHandlerArgs<C>, List<Message>> makeFn(BiConsumer<CommandMessage<C>, CommandReplyToken> handler) {
    return args -> {
      handler.accept(args.getCommandMessage(), args.getCommandReplyInfo());
      return Collections.emptyList();
    };
  }

  public <C extends Command> CommandHandlersBuilder onMessage(Class<C> commandClass,
                                              Consumer<CommandMessage<C>> handler) {
    this.handlers.add(new CommandHandler(channel, resource, commandClass, args ->  {
      handler.accept(args.getCommandMessage());
      return Collections.emptyList();
    }));
    return this;
  }

  public CommandHandlers build() {
    return new CommandHandlers(handlers);
  }
}
