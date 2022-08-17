package io.eventuate.tram.commands;

import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.inmemory.InMemoryMessaging;
import org.junit.Before;

public abstract class AbstractCommandDispatchingTests {

    protected final InMemoryMessaging inMemoryMessaging = InMemoryMessaging.make();
    protected InMemoryCommands inMemoryCommands;
    protected String channel = "myChannel";


    @Before
    public void setup() {

        CommandHandlers commandHandlers = defineCommandHandlers();

        inMemoryCommands = InMemoryCommands.make(commandHandlers, inMemoryMessaging);
    }

    public abstract CommandHandlers defineCommandHandlers();
}
