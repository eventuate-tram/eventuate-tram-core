package io.eventuate.tram.spring.commands.consumer;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.*;
import io.eventuate.tram.commands.consumer.annotations.FailureReply;
import io.eventuate.tram.commands.consumer.annotations.SuccessReply;
import io.eventuate.tram.common.TypeParameterExtractor;
import io.eventuate.tram.messaging.common.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withFailure;
import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withSuccess;

public class EventuateCommandDispatcher implements SmartLifecycle {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final CommandDispatcherFactory commandDispatcherFactory;
  private final List<CommandHandlerInfo> commandHandlers = new ArrayList<>();
  private boolean running = false;
  private List<CommandDispatcher> dispatchers;

  public EventuateCommandDispatcher(CommandDispatcherFactory commandDispatcherFactory) {
    this.commandDispatcherFactory = commandDispatcherFactory;
  }

  public void registerHandlerMethod(CommandHandlerInfo commandHandler) {
    logger.info("Registering command handler method: {}", commandHandler);
    commandHandlers.add(commandHandler);
  }

  public List<CommandHandlerInfo> getCommandHandlers() {
    return commandHandlers;
  }

  @Override
  public void start() {
    logger.info("Starting EventuateCommandDispatcher");
    Map<String, List<CommandHandlerInfo>> groupedCommandHandlers = commandHandlers.stream()
        .collect(Collectors.groupingBy(CommandHandlerInfo::getSubscriberId));
    this.dispatchers = groupedCommandHandlers.entrySet()
        .stream()
        .map(e -> commandDispatcherFactory
            .make(e.getKey(), makeCommandHandlers(e.getValue())))
        .collect(Collectors.toList());
    logger.info("Started EventuateCommandDispatcher {}", dispatchers);
    running = true;
  }

  private static CommandHandlers makeCommandHandlers(List<CommandHandlerInfo> commandHandlers) {
    Map<String, List<CommandHandlerInfo>> groupedByChannel = commandHandlers.stream().collect(Collectors.groupingBy(CommandHandlerInfo::getChannel));
    AtomicReference<CommandHandlersBuilder> builder = new AtomicReference<>();
    groupedByChannel.forEach((channel, handlers) -> {
      builder.set(CommandHandlersBuilder.fromChannel(channel));
      handlers.forEach(ch -> {
        builder.get().onMessage(commandClass(ch.getMethod()), consumerFrom(ch));
      });
    });
    return builder.get().build();
  }

  private static Class<? extends Command> commandClass(Method method) {
    return (Class<? extends Command>) TypeParameterExtractor.extractTypeParameter(method);
  }

  @Override
  public void stop() {
    // No close dispatchers.forEach(CommandDispatcher::);
    running = false;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  private static <T extends Command> Function<CommandMessage<T>, Message> consumerFrom(CommandHandlerInfo handler) {
    return cm -> {
      Object reply = invokeMethod(handler, cm);
      return reply == null || reply instanceof Message ? (Message) reply : makeReplyMessage(reply);
    };
  }

  private static <T extends Command> Object invokeMethod(CommandHandlerInfo handler, CommandMessage<T> cm) {
    Object reply;
    try {
      reply = handler.getMethod().invoke(handler.getTarget(), cm);
    } catch (Exception e) {
      throw new RuntimeException("Error invoking command handler method", e);
    }
    return reply;
  }

  private static Message makeReplyMessage(Object reply) {
    Class<?> clasz = reply.getClass();
    if (clasz.getAnnotation(SuccessReply.class) != null)
      return withSuccess(reply);
    if (clasz.getAnnotation(FailureReply.class) != null)
      return withFailure(reply);
    throw new RuntimeException("Reply class must be annotated with @SuccessReply or @FailureReply " + reply);
  }
}
