package io.eventuate.tram.commands;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandHandlersBuilder;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.CommandReplyToken;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.testutil.TestMessageConsumer;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;

import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withSuccess;
import static io.eventuate.util.test.async.Eventually.eventually;
import static io.eventuate.util.test.async.Eventually.eventuallyReturning;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@MockitoSettings(strictness = Strictness.WARN)
@ExtendWith(MockitoExtension.class)
public class CommandDispatchingTests extends AbstractCommandDispatchingTests {


  private TestMessageConsumer testMessageConsumer;

  static class TestCommand implements Command {
    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }

  }

  static class TestComplexCommand implements Command {
    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }

  }

  @BeforeEach
  @Override
  public void setup() {
    super.setup();
    testMessageConsumer = TestMessageConsumer.subscribeTo(inMemoryMessaging.messageConsumer, replyTo);
  }

  @Spy
  protected CommandDispatcherTarget target = new CommandDispatcherTarget();

  private static class CommandDispatcherTarget {


    public Message handleCommand(CommandMessage<TestCommand> cm) {
      return withSuccess();
    }

    public void handleComplexCommand(CommandMessage<TestComplexCommand> cm, CommandReplyToken replyToken) {
    }

  }

  @Override
  public CommandHandlers defineCommandHandlers() {
    return CommandHandlersBuilder
            .fromChannel(channel)
            .onMessage(TestCommand.class, target::handleCommand)
            .onComplexMessage(TestComplexCommand.class, target::handleComplexCommand)
            .build();
  }

  String replyTo = "reply-channel";

  @Test
  public void testSendingCommand() {

    String messageId = inMemoryCommands.commandProducer.send(channel, new TestCommand(), replyTo, Collections.emptyMap());
    assertNotNull(messageId);

    eventually(() -> {
      verify(target).handleCommand(any(CommandMessage.class));
      verifyNoMoreInteractions(target);
    });

    eventually(() -> testMessageConsumer.assertHasReplyTo(messageId));

  }

  @Test
  public void testSendingComplexCommand() {

    String messageId = inMemoryCommands.commandProducer.send(channel, new TestComplexCommand(), replyTo, Collections.emptyMap());
    assertNotNull(messageId);


    CommandReplyToken crt = eventuallyReturning(() -> {
      ArgumentCaptor<CommandMessage<TestComplexCommand>> cmCaptor = ArgumentCaptor.forClass(CommandMessage.class);
      ArgumentCaptor<CommandReplyToken> replyInfoCaptor = ArgumentCaptor.forClass(CommandReplyToken.class);

      verify(target).handleComplexCommand(cmCaptor.capture(), replyInfoCaptor.capture());
      verifyNoMoreInteractions(target);

      return replyInfoCaptor.getValue();
    });

    List<Message> replies = inMemoryCommands.commandReplyProducer.sendReplies(crt, withSuccess());

    eventually(() -> {
      testMessageConsumer.assertHasReplyTo(messageId);
      testMessageConsumer.assertContainsMessage(replies.get(0));
    });

  }

}
