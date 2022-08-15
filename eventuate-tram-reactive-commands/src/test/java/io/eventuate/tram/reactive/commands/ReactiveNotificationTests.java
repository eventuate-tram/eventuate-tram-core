package io.eventuate.tram.reactive.commands;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlers;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlersBuilder;
import io.eventuate.util.test.async.Eventually;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class ReactiveNotificationTests extends ReactiveAbstractCommandDispatchingTests {

    @Spy
    private CommandDispatcherTestTarget target = new CommandDispatcherTestTarget();

    static class TestNotification implements Command {
        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

    }

    static class CommandDispatcherTestTarget {


        public Mono<Void> handleNotification(CommandMessage<TestNotification> cm) {
            return null;
        }

    }

    public ReactiveCommandHandlers defineCommandHandlers() {
        return ReactiveCommandHandlersBuilder
                .fromChannel(channel)
                .onNotification(TestNotification.class, target::handleNotification)
                .build();
    }


    @Test
    public void testSendingNotification() {

        String messageId = commandProducer.sendNotification(channel, new TestNotification(), Collections.emptyMap()).block();
        assertNotNull(messageId);

        Eventually.eventually(() -> {
            verify(target).handleNotification(any(CommandMessage.class));
            verifyNoMoreInteractions(target);

        });

    }
}
