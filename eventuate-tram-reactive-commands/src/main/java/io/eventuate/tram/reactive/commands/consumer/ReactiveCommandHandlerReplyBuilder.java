package io.eventuate.tram.reactive.commands.consumer;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.CommandReplyOutcome;
import io.eventuate.tram.commands.common.Failure;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.commands.common.Success;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import reactor.core.publisher.Mono;

public class ReactiveCommandHandlerReplyBuilder {

  private static <T> Mono<Message> with(T reply, CommandReplyOutcome outcome) {
    MessageBuilder messageBuilder = MessageBuilder
            .withPayload(JSonMapper.toJson(reply))
            .withHeader(ReplyMessageHeaders.REPLY_OUTCOME, outcome.name())
            .withHeader(ReplyMessageHeaders.REPLY_TYPE, reply.getClass().getName());

    return Mono.defer(() -> Mono.just(messageBuilder.build()));
  }

  public static Mono<Message> withSuccess(Object reply) {
    return with(reply, CommandReplyOutcome.SUCCESS);
  }

  public static Mono<Message> withSuccess() {
    return withSuccess(new Success());
  }

  public static Mono<Message> withFailure() {
    return withFailure(new Failure());
  }
  public static Mono<Message> withFailure(Object reply) {
    return with(reply, CommandReplyOutcome.FAILURE);
  }

}
