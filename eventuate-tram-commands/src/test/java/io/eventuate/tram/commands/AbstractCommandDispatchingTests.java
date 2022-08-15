package io.eventuate.tram.commands;

import io.eventuate.tram.commands.common.DefaultCommandNameMapping;
import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.commands.consumer.CommandDispatcherFactory;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandReplyProducer;
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
import org.junit.Before;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractCommandDispatchingTests {

    protected String channel = "myChannel";
    protected CommandProducer commandProducer;
    protected MessageConsumer messageConsumer;
    protected CommandReplyProducer commandReplyProducer;


    @Before
    public void setup() {
        InMemoryMessageConsumer inMemoryMessageConsumer = new InMemoryMessageConsumer();
        EventuateTransactionSynchronizationManager eventuateTransactionSynchronizationManager = mock(EventuateTransactionSynchronizationManager.class);
        when(eventuateTransactionSynchronizationManager.isTransactionActive()).thenReturn(false);

        ChannelMapping channelMapping = new DefaultChannelMapping.DefaultChannelMappingBuilder().build();
        MessageProducer messageProducer = new MessageProducerImpl(new MessageInterceptor[0], channelMapping,
                new InMemoryMessageProducer(inMemoryMessageConsumer, eventuateTransactionSynchronizationManager));

        messageConsumer = new MessageConsumerImpl(channelMapping, inMemoryMessageConsumer, new DecoratedMessageHandlerFactory(Collections.emptyList()));

        DefaultCommandNameMapping commandNameMapping = new DefaultCommandNameMapping();
        commandProducer = new CommandProducerImpl(messageProducer, commandNameMapping);

        CommandHandlers commandHandlers = defineCommandHandlers();
        commandReplyProducer = new CommandReplyProducer(messageProducer);
        CommandDispatcherFactory commandDispatcherFactory = new CommandDispatcherFactory(messageConsumer, messageProducer, commandNameMapping, commandReplyProducer);
        CommandDispatcher commandDispatcher = commandDispatcherFactory.make("subscriberId", commandHandlers);
        commandDispatcher.initialize();
    }

    public abstract CommandHandlers defineCommandHandlers();
}
