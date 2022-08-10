package io.eventuate.tram.reactive.commands;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.DefaultCommandNameMapping;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.consumer.common.reactive.DecoratedReactiveMessageHandlerFactory;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumerImpl;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.DefaultChannelMapping;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandDispatcher;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlers;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlersBuilder;
import io.eventuate.tram.reactive.commands.producer.ReactiveCommandProducer;
import io.eventuate.tram.reactive.commands.producer.ReactiveCommandProducerImpl;
import io.eventuate.tram.reactive.inmemory.ReactiveInMemoryMessageConsumer;
import io.eventuate.tram.reactive.inmemory.ReactiveInMemoryMessageProducer;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import io.eventuate.util.test.async.Eventually;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Before;
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
public class ReactiveNotificationTests {

    private final String channel = "myChannel";
    private ReactiveCommandProducer commandProducer;

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

    public ReactiveCommandHandlers defineCommandHandlers(CommandDispatcherTestTarget target) {
        return ReactiveCommandHandlersBuilder
                .fromChannel(channel)
                .onNotification(TestNotification.class, target::handleNotification)
                .build();
    }

    @Before
    public void setup() {
        ReactiveInMemoryMessageConsumer inMemoryMessageConsumer = new ReactiveInMemoryMessageConsumer();

        ChannelMapping channelMapping = new DefaultChannelMapping.DefaultChannelMappingBuilder().build();
        ReactiveMessageProducer messageProducer = new ReactiveMessageProducer(new MessageInterceptor[0], channelMapping,
                new ReactiveInMemoryMessageProducer(inMemoryMessageConsumer));

        DefaultCommandNameMapping commandNameMapping = new DefaultCommandNameMapping();
        commandProducer = new ReactiveCommandProducerImpl(messageProducer, commandNameMapping);

        ReactiveCommandHandlers commandHandlers = defineCommandHandlers(target);

        ReactiveMessageConsumer messageConsumer = new ReactiveMessageConsumerImpl(channelMapping, inMemoryMessageConsumer, new DecoratedReactiveMessageHandlerFactory(Collections.emptyList()));

        ReactiveCommandDispatcher commandDispatcher = new ReactiveCommandDispatcher("subscriberId", commandHandlers, messageConsumer, messageProducer);
        commandDispatcher.initialize();
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
