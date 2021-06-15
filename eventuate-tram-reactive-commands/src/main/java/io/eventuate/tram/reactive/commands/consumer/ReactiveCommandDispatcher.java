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
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;

public class ReactiveCommandDispatcher {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private String commandDispatcherId;

  private ReactiveCommandHandlers commandHandlers;

  private ReactiveMessageConsumer messageConsumer;

  private ReactiveMessageProducer messageProducer;

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

    Publisher<List<Message>> replies;

    try {
      CommandMessage cm = new CommandMessage(message.getId(), commandHandlerParams.getCommand(), commandHandlerParams.getCorrelationHeaders(), message);
      replies = m.invokeMethod(cm, commandHandlerParams.getPathVars());
      logger.trace("Generated replies {} {} {}", commandDispatcherId, message, replies);
    } catch (Exception e) {
      logger.error("Generated error {} {} {}", commandDispatcherId, message, e.getClass().getName());
      logger.error("Generated error", e);
      return handleException(commandHandlerParams, m, e, commandHandlerParams.getDefaultReplyChannel());
    }

    if (replies != null) {
      return sendReplies(commandHandlerParams.getCorrelationHeaders(), replies, commandHandlerParams.getDefaultReplyChannel());
    } else {
      return Mono.defer(Mono::empty);
    }
  }

  private String destination(Optional<String> defaultReplyChannel) {
    return defaultReplyChannel.orElseGet(() -> {
      throw new RuntimeException();
    });
  }

  private Publisher<List<Message>> handleException(CommandHandlerParams commandHandlerParams,
                                                   ReactiveCommandHandler reactiveCommandHandler,
                                                   Throwable cause,
                                                   Optional<String> defaultReplyChannel) {

    Optional<ReactiveCommandExceptionHandler> exceptionHandler =
            commandHandlers.findExceptionHandler(reactiveCommandHandler, cause);

    logger.info("Handler for {} is {}", cause.getClass(), exceptionHandler);

    Publisher<List<Message>> replies =
            exceptionHandler
                    .map(eh -> eh.invoke(cause))
                    .orElse(Mono.just(singletonList(MessageBuilder.withPayload(JSonMapper.toJson(new Failure())).build())));

    return sendReplies(commandHandlerParams.getCorrelationHeaders(), replies, defaultReplyChannel);
  }

  private Mono<List<Message>> sendReplies(Map<String, String> correlationHeaders,
                                          Publisher<List<Message>> replies,
                                          Optional<String> defaultReplyChannel) {
    return Mono
            .from(replies)
            .flatMap(rs -> {

              List<Mono<Message>> sentReplies = new ArrayList<>();

              for (Message reply : rs) {
                Message transformedReply = MessageBuilder
                        .withMessage(reply)
                        .withExtraHeaders("", correlationHeaders)
                        .build();

                sentReplies.add(messageProducer.send(destination(defaultReplyChannel), transformedReply));
              }

              return Mono.zip(sentReplies, objects -> {
                ArrayList<Message> messages = new ArrayList<>();

                for (Object o : objects) {
                  messages.add((Message)o);
                }

                return messages;
              });
            });
  }
}
