package io.eventuate.tram.reactive.commands;

import io.eventuate.tram.commands.common.DefaultCommandNameMapping;
import io.eventuate.tram.consumer.common.reactive.DecoratedReactiveMessageHandlerFactory;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumerImpl;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.DefaultChannelMapping;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandDispatcher;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlers;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandReplyProducer;
import io.eventuate.tram.reactive.commands.producer.ReactiveCommandProducer;
import io.eventuate.tram.reactive.commands.producer.ReactiveCommandProducerImpl;
import io.eventuate.tram.reactive.inmemory.ReactiveInMemoryMessageConsumer;
import io.eventuate.tram.reactive.inmemory.ReactiveInMemoryMessageProducer;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;

public abstract class ReactiveAbstractCommandDispatchingTests {

    protected final String channel = "myChannel";
    protected ReactiveCommandProducer commandProducer;

    protected ReactiveCommandReplyProducer commandReplyProducer;
    protected ReactiveMessageConsumer messageConsumer;

    @BeforeEach
    public void setup() {
        ReactiveInMemoryMessageConsumer inMemoryMessageConsumer = new ReactiveInMemoryMessageConsumer();

        ChannelMapping channelMapping = new DefaultChannelMapping.DefaultChannelMappingBuilder().build();
        ReactiveMessageProducer messageProducer = new ReactiveMessageProducer(new MessageInterceptor[0], channelMapping,
                new ReactiveInMemoryMessageProducer(inMemoryMessageConsumer));

        DefaultCommandNameMapping commandNameMapping = new DefaultCommandNameMapping();
        commandProducer = new ReactiveCommandProducerImpl(messageProducer, commandNameMapping);

        ReactiveCommandHandlers commandHandlers = defineCommandHandlers();

        messageConsumer = new ReactiveMessageConsumerImpl(channelMapping, inMemoryMessageConsumer, new DecoratedReactiveMessageHandlerFactory(Collections.emptyList()));

        commandReplyProducer = new ReactiveCommandReplyProducer(messageProducer);

        ReactiveCommandDispatcher commandDispatcher = new ReactiveCommandDispatcher("subscriberId", commandHandlers, messageConsumer, commandReplyProducer);
        commandDispatcher.initialize();
    }

    protected abstract ReactiveCommandHandlers defineCommandHandlers();

}
