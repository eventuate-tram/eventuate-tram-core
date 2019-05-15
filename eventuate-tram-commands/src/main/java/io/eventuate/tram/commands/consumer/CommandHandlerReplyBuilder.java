package io.eventuate.tram.commands.consumer;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.commands.common.CommandReplyOutcome;
import io.eventuate.tram.commands.common.Failure;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.commands.common.Success;
import io.eventuate.tram.messaging.producer.MessageBuilder;

public class CommandHandlerReplyBuilder {


  private static <T> Message with(T reply, CommandReplyOutcome outcome) {
    MessageBuilder messageBuilder = MessageBuilder
            .withPayload(JSonMapper.toJson(reply))
            .withHeader(ReplyMessageHeaders.REPLY_OUTCOME, outcome.name())
            .withHeader(ReplyMessageHeaders.REPLY_TYPE, reply.getClass().getName());
    return messageBuilder.build();
  }

  public static Message withSuccess(Object reply) {
    return with(reply, CommandReplyOutcome.SUCCESS);
  }

  public static Message withSuccess() {
    return withSuccess(new Success());
  }

  public static Message withFailure() {
    return withFailure(new Failure());
  }
  public static Message withFailure(Object reply) {
    return with(reply, CommandReplyOutcome.FAILURE);
  }

}
