package io.eventuate.tram.reactive.commands.consumer;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.Failure;
import io.eventuate.tram.commands.consumer.CommandHandlerParams;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;

public class ReactiveCommandDispatcher {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final String commandDispatcherId;

  private final ReactiveCommandHandlers commandHandlers;

  private final ReactiveMessageConsumer messageConsumer;

  private final ReactiveMessageProducer messageProducer;

  public ReactiveCommandDispatcher(String commandDispatcherId,
                                   ReactiveCommandHandlers commandHandlers,
                                   ReactiveMessageConsumer messageConsumer,
                                   ReactiveMessageProducer messageProducer) {
    this.commandDispatcherId = commandDispatcherId;
    this.commandHandlers = commandHandlers;
    this.messageConsumer = messageConsumer;
    this.messageProducer = messageProducer;
  }

  @PostConstruct
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

    Flux<Message> replies;

    try {
      CommandMessage<?> cm = new CommandMessage<>(message.getId(), commandHandlerParams.getCommand(), commandHandlerParams.getCorrelationHeaders(), message);
      replies = Flux.from(invoke(m, cm, commandHandlerParams));
      logger.trace("Generated replies {} {} {}", commandDispatcherId, message, replies);
    } catch (Exception e) {
      logger.error("Generated error {} {} {}", commandDispatcherId, message, e.getClass().getName());
      logger.error("Generated error", e);
      return handleException(commandHandlerParams, m, e, commandHandlerParams.getDefaultReplyChannel());
    }

    return sendReplies(commandHandlerParams.getCorrelationHeaders(), replies, commandHandlerParams.getDefaultReplyChannel()).then();
  }

  protected Publisher<Message> invoke(ReactiveCommandHandler m,
                                      CommandMessage<?> cm,
                                 CommandHandlerParams commandHandlerParams) {
    return m.invokeMethod(cm, commandHandlerParams.getPathVars());
  }

  private String destination(Optional<String> defaultReplyChannel) {
    return defaultReplyChannel.orElseThrow(RuntimeException::new);
  }

  private Flux<Message> handleException(CommandHandlerParams commandHandlerParams,
                                                   ReactiveCommandHandler reactiveCommandHandler,
                                                   Throwable cause,
                                                   Optional<String> defaultReplyChannel) {

    Optional<ReactiveCommandExceptionHandler> exceptionHandler =
            commandHandlers.findExceptionHandler(reactiveCommandHandler, cause);

    logger.info("Handler for {} is {}", cause.getClass(), exceptionHandler);

    Flux<Message> replies =
            exceptionHandler
                    .map(eh -> Flux.from(eh.invoke(cause)))
                    .orElse(Flux.fromIterable(singletonList(MessageBuilder.withPayload(JSonMapper.toJson(new Failure())).build())));

    return sendReplies(commandHandlerParams.getCorrelationHeaders(), replies, defaultReplyChannel);
  }

  private Flux<Message> sendReplies(Map<String, String> correlationHeaders,
                                          Flux<Message> replies,
                                          Optional<String> defaultReplyChannel) {
    return replies
            .flatMap(reply -> {
              Message transformedReply = MessageBuilder
                      .withMessage(reply)
                      .withExtraHeaders("", correlationHeaders)
                      .build();

              return messageProducer.send(destination(defaultReplyChannel), transformedReply);
            });
  }
}
