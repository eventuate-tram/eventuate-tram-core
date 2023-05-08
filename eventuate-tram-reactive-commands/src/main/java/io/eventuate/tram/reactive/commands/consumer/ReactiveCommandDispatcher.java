package io.eventuate.tram.reactive.commands.consumer;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.Failure;
import io.eventuate.tram.commands.consumer.*;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.Optional;

import static java.util.Collections.singletonList;

public class ReactiveCommandDispatcher {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final String commandDispatcherId;

  private final ReactiveCommandHandlers commandHandlers;

  private final ReactiveMessageConsumer messageConsumer;
  private final ReactiveCommandReplyProducer commandReplyProducer;


  public ReactiveCommandDispatcher(String commandDispatcherId,
                                   ReactiveCommandHandlers commandHandlers,
                                   ReactiveMessageConsumer messageConsumer,
                                   ReactiveCommandReplyProducer commandReplyProducer) {
    this.commandDispatcherId = commandDispatcherId;
    this.commandHandlers = commandHandlers;
    this.messageConsumer = messageConsumer;
    this.commandReplyProducer = commandReplyProducer;
  }

  public void initialize() {
    messageConsumer.subscribe(commandDispatcherId, commandHandlers.getChannels(), this::messageHandler);
  }

  public Publisher<?> messageHandler(Message message) {
    logger.trace("Received message {} {}", commandDispatcherId, message);

    Optional<ReactiveCommandHandler> possibleMethod = commandHandlers.findTargetMethod(message);

    if (!possibleMethod.isPresent()) {
      throw new RuntimeException("No method for " + message);
    }

    ReactiveCommandHandler m = possibleMethod.get();

    CommandHandlerParams commandHandlerParams = new CommandHandlerParams(message, m.getCommandClass(), m.getResource());
    CommandReplyToken commandReplyToken = new CommandReplyToken(commandHandlerParams.getCorrelationHeaders(), commandHandlerParams.getDefaultReplyChannel().orElse(null));

    Flux<Message> replies;

    try {
      CommandMessage<?> cm = new CommandMessage<>(message.getId(), commandHandlerParams.getCommand(), commandHandlerParams.getCorrelationHeaders(), message);
      replies = Flux.from(invoke(m, cm, commandHandlerParams, commandReplyToken));
      logger.trace("Generated replies {} {} {}", commandDispatcherId, message, replies);
    } catch (Exception e) {
      logger.error("Generated error {} {} {}", commandDispatcherId, message, e.getClass().getName());
      logger.error("Generated error", e);
      return handleException(m, e, commandReplyToken);
    }

    return commandReplyProducer.sendReplies(commandReplyToken, replies).then();
  }

  protected Publisher<Message> invoke(ReactiveCommandHandler m,
                                      CommandMessage<?> cm,
                                      CommandHandlerParams commandHandlerParams, CommandReplyToken commandReplyToken) {
    return m.invokeMethod(new CommandHandlerArgs(cm, new PathVariables(commandHandlerParams.getPathVars()), commandReplyToken));
  }

  private Flux<Message> handleException(ReactiveCommandHandler reactiveCommandHandler,
                                        Throwable cause,
                                        CommandReplyToken commandReplyToken) {

    Optional<ReactiveCommandExceptionHandler> exceptionHandler =
            commandHandlers.findExceptionHandler(reactiveCommandHandler, cause);

    logger.info("Handler for {} is {}", cause.getClass(), exceptionHandler);

    Flux<Message> replies =
            exceptionHandler
                    .map(eh -> Flux.from(eh.invoke(cause)))
                    .orElse(Flux.fromIterable(singletonList(MessageBuilder.withPayload(JSonMapper.toJson(new Failure())).build())));

    return commandReplyProducer.sendReplies(commandReplyToken, replies);
  }

}
