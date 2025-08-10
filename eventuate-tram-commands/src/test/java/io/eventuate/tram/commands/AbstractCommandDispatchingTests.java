package io.eventuate.tram.commands;

import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.inmemory.InMemoryMessaging;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractCommandDispatchingTests {

    protected final InMemoryMessaging inMemoryMessaging = InMemoryMessaging.make();
    protected InMemoryCommands inMemoryCommands;
    protected String channel = "myChannel";


    @BeforeEach
    public void setup() {

        CommandHandlers commandHandlers = defineCommandHandlers();

        inMemoryCommands = InMemoryCommands.make(commandHandlers, inMemoryMessaging);
    }

    public abstract CommandHandlers defineCommandHandlers();
}
