package io.eventuate.tram.commands;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandHandlersBuilder;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.CommandReplyToken;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.testutil.TestMessageConsumer;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withSuccess;
import static io.eventuate.util.test.async.Eventually.eventually;
import static io.eventuate.util.test.async.Eventually.eventuallyReturning;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
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

    @Override
    public void setup() {
        super.setup();
        testMessageConsumer = TestMessageConsumer.subscribeTo(messageConsumer, replyTo);
    }

    @Spy
    protected CommandDispatchingTests.CommandDispatcherTestTarget target = new CommandDispatchingTests.CommandDispatcherTestTarget();

    static class CommandDispatcherTestTarget {


        public Message handleCommand(CommandMessage<TestCommand> cm) {
            return withSuccess();
        }

        public void handleComplexCommand(CommandMessage<TestComplexCommand> cm, CommandReplyToken replyInfo) {
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

        String messageId = commandProducer.send(channel, new TestCommand(), replyTo, Collections.emptyMap());
        assertNotNull(messageId);

        eventually(() -> {
            verify(target).handleCommand(any(CommandMessage.class));
            verifyNoMoreInteractions(target);
        });

        eventually(() -> {
            testMessageConsumer.assertHasReplyTo(messageId);
        });

    }

    @Test
    public void testSendingComplexCommand() {

        String messageId = commandProducer.send(channel, new TestComplexCommand(), replyTo, Collections.emptyMap());
        assertNotNull(messageId);


        CommandReplyToken cri = eventuallyReturning(() -> {
            ArgumentCaptor<CommandMessage<TestComplexCommand>> cmCaptor = ArgumentCaptor.forClass(CommandMessage.class);
            ArgumentCaptor<CommandReplyToken> replyInfoCaptor = ArgumentCaptor.forClass(CommandReplyToken.class);

            verify(target).handleComplexCommand(cmCaptor.capture(), replyInfoCaptor.capture());
            verifyNoMoreInteractions(target);

            return replyInfoCaptor.getValue();
        });

        List<Message> replies = commandReplyProducer.sendReplies(cri, Collections.singletonList(withSuccess()));

        eventually(() -> {
            testMessageConsumer.assertHasReplyTo(messageId);
            testMessageConsumer.assertContainsMessage(replies.get(0));
        });

    }

}
