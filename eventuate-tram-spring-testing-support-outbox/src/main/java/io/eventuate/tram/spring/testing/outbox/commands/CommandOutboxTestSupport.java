package io.eventuate.tram.spring.testing.outbox.commands;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.commands.common.CommandReplyOutcome;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.spring.testing.outbox.messaging.MessageOutboxTestSupport;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static io.eventuate.util.test.async.Eventually.eventually;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandOutboxTestSupport {

  private final MessageOutboxTestSupport messageOutboxTestSupport;

  public CommandOutboxTestSupport(MessageOutboxTestSupport messageOutboxTestSupport) {
    this.messageOutboxTestSupport = messageOutboxTestSupport;
  }

  public <T extends Command> void assertCommandMessageSent(String channel, Class<T> commandMessageType) {

    List<Message> messages = findMessagesSentToChannel(channel);

    assertThat(messages)
            .hasSize(1)
            .allMatch(reply -> commandMessageType.getName().equals(reply.getRequiredHeader(CommandMessageHeaders.COMMAND_TYPE)));
  }
  public void assertCommandReplyMessageSent(String channel) {

    List<Message> messages = findMessagesSentToChannel(channel);

    assertThat(messages)
            .hasSize(1)
            .allMatch(reply -> CommandReplyOutcome.SUCCESS.name().equals(reply.getRequiredHeader(ReplyMessageHeaders.REPLY_OUTCOME)));
  }

  public <T extends Command> Message assertThatCommandMessageSent(Class<T> commandMessageType, String channel, Predicate<T> predicate) {
    eventually(10, 500, TimeUnit.MILLISECONDS, () -> {
      List<Message> messages = findMessagesSentToChannel(channel);
      boolean found = messages.stream()
          .filter(msg -> commandMessageType.getName().equals(msg.getHeader(CommandMessageHeaders.COMMAND_TYPE).orElse(null)))
          .map(msg -> JSonMapper.fromJson(msg.getPayload(), commandMessageType))
          .anyMatch(predicate);
      assertThat(found)
          .withFailMessage("Expected to find matching %s in channel %s", commandMessageType.getSimpleName(), channel)
          .isTrue();
    });

    List<Message> messages = findMessagesSentToChannel(channel);
    for (int i = messages.size() - 1; i >= 0; i--) {
      Message msg = messages.get(i);
      if (commandMessageType.getName().equals(msg.getHeader(CommandMessageHeaders.COMMAND_TYPE).orElse(null))) {
        T command = JSonMapper.fromJson(msg.getPayload(), commandMessageType);
        if (predicate.test(command)) {
          return msg;
        }
      }
    }
    throw new IllegalStateException("No matching message found");
  }

  public List<Message> findMessagesSentToChannel(String channel) {
    return messageOutboxTestSupport.findMessagesSentToChannel(channel);
  }

  public <T extends Command> List<T> findCommandsOfType(String channel, Class<T> commandType) {
    return findMessagesOfType(channel, commandType).stream()
        .map(msg -> JSonMapper.fromJson(msg.getPayload(), commandType))
        .toList();
  }

  public <T extends Command> List<Message> findMessagesOfType(String channel, Class<T> commandType) {
    return findMessagesSentToChannel(channel).stream()
        .filter(msg -> commandType.getName().equals(msg.getHeader(CommandMessageHeaders.COMMAND_TYPE).orElse(null)))
        .toList();
  }
}
