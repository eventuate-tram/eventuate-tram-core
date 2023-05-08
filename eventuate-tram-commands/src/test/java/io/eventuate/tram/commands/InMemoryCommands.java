package io.eventuate.tram.commands;

import io.eventuate.tram.commands.common.DefaultCommandNameMapping;
import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.commands.consumer.CommandDispatcherFactory;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandReplyProducer;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.commands.producer.CommandProducerImpl;
import io.eventuate.tram.inmemory.InMemoryMessaging;

public class InMemoryCommands {
    public final CommandProducer commandProducer;
    public final CommandReplyProducer commandReplyProducer;

    public InMemoryCommands(CommandProducer commandProducer, CommandReplyProducer commandReplyProducer) {
        this.commandProducer = commandProducer;
        this.commandReplyProducer = commandReplyProducer;
    }

    public static InMemoryCommands make(CommandHandlers commandHandlers, InMemoryMessaging inMemoryMessaging) {
        DefaultCommandNameMapping commandNameMapping = new DefaultCommandNameMapping();
        CommandProducer commandProducer = new CommandProducerImpl(inMemoryMessaging.messageProducer, commandNameMapping);

        CommandReplyProducer commandReplyProducer = new CommandReplyProducer(inMemoryMessaging.messageProducer);
        CommandDispatcherFactory commandDispatcherFactory = new CommandDispatcherFactory(inMemoryMessaging.messageConsumer, commandNameMapping, commandReplyProducer);
        CommandDispatcher commandDispatcher = commandDispatcherFactory.make("subscriberId", commandHandlers);
        return new InMemoryCommands(commandProducer, commandReplyProducer);
    }
}
