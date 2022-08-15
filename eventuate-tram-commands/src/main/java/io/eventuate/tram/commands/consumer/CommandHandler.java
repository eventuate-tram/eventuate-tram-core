package io.eventuate.tram.commands.consumer;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.messaging.common.Message;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;


public class CommandHandler extends AbstractCommandHandler<List<Message>>{

  public <C extends Command> CommandHandler(String channel, Optional<String> resource,
                                            Class<C> commandClass,
                                            Function<CommandHandlerArgs<C>, List<Message>>  handler) {

    super(channel, resource, commandClass, handler);
  }
}
