package io.eventuate.tram.commands;

import io.eventuate.tram.commands.common.DefaultCommandNameMapping;
import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.commands.consumer.CommandDispatcherFactory;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandReplyProducer;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.commands.producer.CommandProducerImpl;
import io.eventuate.tram.inmemory.InMemoryMessagingFactory;

public class InMemoryCommandsFactory {
    public final CommandProducer commandProducer;
    public final CommandReplyProducer commandReplyProducer;

    public InMemoryCommandsFactory(CommandProducer commandProducer, CommandReplyProducer commandReplyProducer) {
        this.commandProducer = commandProducer;
        this.commandReplyProducer = commandReplyProducer;
    }

    public static InMemoryCommandsFactory make(CommandHandlers commandHandlers, InMemoryMessagingFactory inMemoryMessagingFactory) {
        DefaultCommandNameMapping commandNameMapping = new DefaultCommandNameMapping();
        CommandProducer commandProducer = new CommandProducerImpl(inMemoryMessagingFactory.messageProducer, commandNameMapping);

        CommandReplyProducer commandReplyProducer = new CommandReplyProducer(inMemoryMessagingFactory.messageProducer);
        CommandDispatcherFactory commandDispatcherFactory = new CommandDispatcherFactory(inMemoryMessagingFactory.messageConsumer, commandNameMapping, commandReplyProducer);
        CommandDispatcher commandDispatcher = commandDispatcherFactory.make("subscriberId", commandHandlers);
        commandDispatcher.initialize();
        return new InMemoryCommandsFactory(commandProducer, commandReplyProducer);
    }
}