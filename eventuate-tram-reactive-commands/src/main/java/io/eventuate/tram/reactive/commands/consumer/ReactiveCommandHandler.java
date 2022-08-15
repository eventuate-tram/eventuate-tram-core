package io.eventuate.tram.reactive.commands.consumer;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.AbstractCommandHandler;
import io.eventuate.tram.commands.consumer.CommandHandlerArgs;
import io.eventuate.tram.messaging.common.Message;
import org.reactivestreams.Publisher;

import java.util.Optional;
import java.util.function.Function;


public class ReactiveCommandHandler extends AbstractCommandHandler<Publisher<Message>> {

  public <C extends Command> ReactiveCommandHandler(String channel, Optional<String> resource,
                                                    Class<C> commandClass,
                                                    Function<CommandHandlerArgs<C>, Publisher<Message>> handler) {
    super(channel, resource, commandClass, handler);
  }
}
