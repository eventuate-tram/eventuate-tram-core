package io.eventuate.tram.commands.consumer;

import io.eventuate.tram.messaging.common.Message;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;


public class CommandHandler extends AbstractCommandHandler<List<Message>>{

  public <C> CommandHandler(String channel, Optional<String> resource,
                            Class<C> commandClass,
                            BiFunction<CommandMessage<C>, PathVariables, List<Message>> handler) {

    super(channel, resource, commandClass, handler);
  }
}
