package io.eventuate.tram.commands;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.DefaultCommandNameMapping;
import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandHandlersBuilder;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.commands.producer.CommandProducerImpl;
import io.eventuate.tram.consumer.common.DecoratedMessageHandlerFactory;
import io.eventuate.tram.consumer.common.MessageConsumerImpl;
import io.eventuate.tram.inmemory.EventuateTransactionSynchronizationManager;
import io.eventuate.tram.inmemory.InMemoryMessageConsumer;
import io.eventuate.tram.inmemory.InMemoryMessageProducer;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.DefaultChannelMapping;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.messaging.producer.common.MessageProducerImpl;
import io.eventuate.util.test.async.Eventually;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NotificationTests {

    private String channel = "myChannel";
    private CommandProducer commandProducer;

    @Spy
    private CommandDispatcherTestTarget target = new CommandDispatcherTestTarget();

    static class TestNotification implements Command {
        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

    }

    static class CommandDispatcherTestTarget {


        public void handleNotification(CommandMessage<TestNotification> cm) {
        }

    }

    public CommandHandlers defineCommandHandlers(CommandDispatcherTestTarget target) {
        return CommandHandlersBuilder
                .fromChannel(channel)
                .onMessage(TestNotification.class, target::handleNotification)
                .build();
    }

    @Before
    public void setup() {
        InMemoryMessageConsumer inMemoryMessageConsumer = new InMemoryMessageConsumer();
        EventuateTransactionSynchronizationManager eventuateTransactionSynchronizationManager = mock(EventuateTransactionSynchronizationManager.class);
        when(eventuateTransactionSynchronizationManager.isTransactionActive()).thenReturn(false);

        ChannelMapping channelMapping = new DefaultChannelMapping.DefaultChannelMappingBuilder().build();
        MessageProducer messageProducer = new MessageProducerImpl(new MessageInterceptor[0], channelMapping,
                new InMemoryMessageProducer(inMemoryMessageConsumer, eventuateTransactionSynchronizationManager));

        DefaultCommandNameMapping commandNameMapping = new DefaultCommandNameMapping();
        commandProducer = new CommandProducerImpl(messageProducer, commandNameMapping);

        CommandHandlers commandHandlers = defineCommandHandlers(target);

        MessageConsumer messageConsumer = new MessageConsumerImpl(channelMapping, inMemoryMessageConsumer, new DecoratedMessageHandlerFactory(Collections.emptyList()));

        CommandDispatcher commandDispatcher = new CommandDispatcher("subscriberId", commandHandlers, messageConsumer, messageProducer, commandNameMapping);
        commandDispatcher.initialize();
    }

    @Test
    public void testSendingNotification() {

        String messageId = commandProducer.sendNotification(channel, new TestNotification(), Collections.emptyMap());
        assertNotNull(messageId);

        Eventually.eventually(() -> {
            verify(target).handleNotification(any(CommandMessage.class));
            verifyNoMoreInteractions(target);

        });

    }
}
