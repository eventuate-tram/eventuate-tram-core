package io.eventuate.tram.commands.consumer;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.commands.common.Failure;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;

public class CommandDispatcher {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private String commandDispatcherId;

  private CommandHandlers commandHandlers;

  private MessageConsumer messageConsumer;

  private MessageProducer messageProducer;
  private CommandNameMapping commandNameMapping;

  public CommandDispatcher(String commandDispatcherId,
                           CommandHandlers commandHandlers,
                           MessageConsumer messageConsumer,
                           MessageProducer messageProducer, CommandNameMapping commandNameMapping) {
    this.commandDispatcherId = commandDispatcherId;
    this.commandHandlers = commandHandlers;
    this.messageConsumer = messageConsumer;
    this.messageProducer = messageProducer;
    this.commandNameMapping = commandNameMapping;
  }

  @PostConstruct
  public void initialize() {
    messageConsumer.subscribe(commandDispatcherId,
            commandHandlers.getChannels(),
            this::messageHandler);
  }

  public void messageHandler(Message message) {
    logger.trace("Received message {} {}", commandDispatcherId, message);

    message.setHeader(CommandMessageHeaders.COMMAND_TYPE,
            commandNameMapping.externalCommandTypeToCommandClassName(message.getRequiredHeader(CommandMessageHeaders.COMMAND_TYPE)));

    Optional<CommandHandler> possibleMethod = commandHandlers.findTargetMethod(message);
    if (!possibleMethod.isPresent()) {
      throw new RuntimeException("No method for " + message);
    }

    CommandHandler m = possibleMethod.get();

    CommandHandlerParams commandHandlerParams = new CommandHandlerParams(message, m.getCommandClass(), m.getResource());

    List<Message> replies;
    try {
      CommandMessage cm = new CommandMessage(message.getId(),
              commandHandlerParams.getCommand(),
              commandHandlerParams.getCorrelationHeaders(),
              message);

      replies = invoke(m, cm, commandHandlerParams.getPathVars());
      logger.trace("Generated replies {} {} {}", commandDispatcherId, message, replies);
    } catch (Exception e) {
      logger.error("Generated error {} {} {}", commandDispatcherId, message, e.getClass().getName());
      logger.error("Generated error", e);
      handleException(commandHandlerParams, m, e, commandHandlerParams.getDefaultReplyChannel());
      return;
    }

    if (replies != null) {
      sendReplies(commandHandlerParams.getCorrelationHeaders(), replies, commandHandlerParams.getDefaultReplyChannel());
    } else {
      logger.trace("Null replies - not publishing");
    }
  }

  protected List<Message> invoke(CommandHandler commandHandler, CommandMessage cm, Map<String, String> pathVars) {
    return commandHandler.invokeMethod(cm, pathVars);
  }

  private void sendReplies(Map<String, String> correlationHeaders, List<Message> replies, Optional<String> defaultReplyChannel) {
    for (Message reply : replies)
      messageProducer.send(destination(defaultReplyChannel),
              MessageBuilder
                      .withMessage(reply)
                      .withExtraHeaders("", correlationHeaders)
                      .build());
  }

  private String destination(Optional<String> defaultReplyChannel) {
    return defaultReplyChannel.orElseGet(() -> {
      throw new RuntimeException();
    });
  }

  private void handleException(CommandHandlerParams commandHandlerParams,
                               CommandHandler commandHandler,
                               Throwable cause,
                               Optional<String> defaultReplyChannel) {
    Optional<CommandExceptionHandler> m = commandHandlers.findExceptionHandler(commandHandler, cause);

    logger.info("Handler for {} is {}", cause.getClass(), m);

    List<Message> replies = m
            .map(handler -> handler.invoke(cause))
            .orElseGet(() -> singletonList(MessageBuilder.withPayload(JSonMapper.toJson(new Failure())).build()));

    sendReplies(commandHandlerParams.getCorrelationHeaders(), replies, defaultReplyChannel);
  }
}
