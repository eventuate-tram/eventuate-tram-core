package io.eventuate.tram.reactive.commands;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.CommandReplyToken;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlers;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlersBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
public class ReactiveCommandDispatchingTests extends ReactiveAbstractCommandDispatchingTests {



    private ReactiveTestMessageConsumer testMessageConsumer;

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
        testMessageConsumer = ReactiveTestMessageConsumer.subscribeTo(messageConsumer, replyTo);
    }

    @Spy
    protected CommandDispatcherTestTarget target = new CommandDispatcherTestTarget();

    static class CommandDispatcherTestTarget {


        public Mono<Message> handleCommand(CommandMessage<TestCommand> cm) {
            return Mono.just(withSuccess());
        }

        public Mono<Void> handleComplexCommand(CommandMessage<TestComplexCommand> cm, CommandReplyToken replyInfo) {
            return Mono.empty();
        }

    }

    @Override
    public ReactiveCommandHandlers defineCommandHandlers() {
        return ReactiveCommandHandlersBuilder
                .fromChannel(channel)
                .onMessage(TestCommand.class, target::handleCommand)
                .onComplexMessage(TestComplexCommand.class, target::handleComplexCommand)
                .build();
    }

    String replyTo = "reply-channel";

    @Test
    public void testSendingCommand() {

        String messageId = commandProducer.send(channel, new TestCommand(), replyTo, Collections.emptyMap()).block();
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

        String messageId = commandProducer.send(channel, new TestComplexCommand(), replyTo, Collections.emptyMap()).block();
        assertNotNull(messageId);


        CommandReplyToken cri = eventuallyReturning(() -> {
            ArgumentCaptor<CommandMessage<TestComplexCommand>> cmCaptor = ArgumentCaptor.forClass(CommandMessage.class);
            ArgumentCaptor<CommandReplyToken> replyInfoCaptor = ArgumentCaptor.forClass(CommandReplyToken.class);

            verify(target).handleComplexCommand(cmCaptor.capture(), replyInfoCaptor.capture());
            verifyNoMoreInteractions(target);

            return replyInfoCaptor.getValue();
        });

        List<Message> replies = commandReplyProducer.sendReplies(cri, Flux.just(withSuccess())).buffer().blockFirst();

        eventually(() -> {
            testMessageConsumer.assertHasReplyTo(messageId);
            testMessageConsumer.assertContainsMessage(replies.get(0));
        });

    }

}
