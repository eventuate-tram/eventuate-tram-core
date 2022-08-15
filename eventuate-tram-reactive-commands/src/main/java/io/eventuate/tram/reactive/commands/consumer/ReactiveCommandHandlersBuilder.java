package io.eventuate.tram.reactive.commands.consumer;


import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandHandlerArgs;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.PathVariables;
import io.eventuate.tram.messaging.common.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ReactiveCommandHandlersBuilder {
  private String channel;
  private Optional<String> resource = Optional.empty();

  private final List<ReactiveCommandHandler> handlers = new ArrayList<>();

  public static ReactiveCommandHandlersBuilder fromChannel(String channel) {
    return new ReactiveCommandHandlersBuilder().andFromChannel(channel);
  }

  private ReactiveCommandHandlersBuilder andFromChannel(String channel) {
    this.channel = channel;
    return this;
  }

  public ReactiveCommandHandlersBuilder resource(String resource) {
    this.resource = Optional.of(resource);
    return this;
  }

  public <C extends Command> ReactiveCommandHandlersBuilder onMessage(Class<C> commandClass,
                                                                      BiFunction<CommandMessage<C>, PathVariables, Publisher<Message>> handler) {

    this.handlers.add(new ReactiveCommandHandler(channel, resource, commandClass, CommandHandlerArgs.makeFn(handler)));
    return this;
  }


  public <C extends Command> ReactiveCommandHandlersBuilder onMessage(Class<C> commandClass,
                                                      Function<CommandMessage<C>, Publisher<Message>> handler) {

    return onMessage(commandClass, (c, pv) -> handler.apply(c));
  }

  public <C extends Command> ReactiveCommandHandlersBuilder onNotification(Class<C> commandClass,
                                                      Function<CommandMessage<C>, Publisher<Void>> handler) {

    BiFunction<CommandMessage<C>, PathVariables, Publisher<Message>> convertedHandler = (c, pv) -> Mono.from(handler.apply(c)).flatMap(x -> Mono.<Message>empty());
    return onMessage(commandClass, convertedHandler);
  }

  public ReactiveCommandHandlers build() {
    return new ReactiveCommandHandlers(handlers);
  }
}
