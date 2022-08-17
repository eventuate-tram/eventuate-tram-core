package io.eventuate.tram.commands;

import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.inmemory.InMemoryMessagingFactory;
import org.junit.Before;

public abstract class AbstractCommandDispatchingTests {

    protected final InMemoryMessagingFactory inMemoryMessagingFactory = InMemoryMessagingFactory.make();
    protected InMemoryCommandsFactory inMemoryCommandsFactory;
    protected String channel = "myChannel";


    @Before
    public void setup() {

        CommandHandlers commandHandlers = defineCommandHandlers();

        inMemoryCommandsFactory = InMemoryCommandsFactory.make(commandHandlers, inMemoryMessagingFactory);
    }

    public abstract CommandHandlers defineCommandHandlers();
}
