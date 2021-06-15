package io.eventuate.tram.reactive.commands.consumer;

import io.eventuate.tram.commands.consumer.AbstractCommandHandler;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.PathVariables;
import io.eventuate.tram.messaging.common.Message;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;


public class ReactiveCommandHandler extends AbstractCommandHandler<Publisher<List<Message>>> {

  public <C> ReactiveCommandHandler(String channel, Optional<String> resource,
                                    Class<C> commandClass,
                                    BiFunction<CommandMessage<C>, PathVariables, Publisher<List<Message>>> handler) {
    super(channel, resource, commandClass, handler);
  }
}
