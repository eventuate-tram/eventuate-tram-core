package io.eventuate.tram.spring.commands.consumer;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.spring.commands.consumer.customersandorders.commands.ReserveCreditCommand;
import org.junit.Test;

import java.lang.reflect.Method;

import static junit.framework.TestCase.assertEquals;

public class CommandClassExtractorTest {

    public Message handleCommand(CommandMessage<ReserveCreditCommand> cm) {
        return null;
    }

    public void handleCommandWithoutGenericType(String notACommand) {
    }

    public void handleCommandWithWrongType(CommandMessage<String> cm) {
    }

    @Test
    public void shouldExtractCommandClass() throws Exception {
        Method method = getClass().getMethod("handleCommand", CommandMessage.class);
        Class<? extends Command> commandClass = CommandClassExtractor.extractCommandClass(method);
        assertEquals(ReserveCreditCommand.class, commandClass);
    }

    @Test
    public void shouldFailForMethodWithoutParameters() throws Exception {
        Method method = getClass().getMethod("handleCommandWithoutGenericType", String.class);
        assertThrows(IllegalArgumentException.class, () -> CommandClassExtractor.extractCommandClass(method));
    }

    @Test
    public void shouldFailForNonCommandType() throws Exception {
        Method method = getClass().getMethod("handleCommandWithWrongType", CommandMessage.class);
        assertThrows(IllegalArgumentException.class, () -> CommandClassExtractor.extractCommandClass(method));
    }

    private void assertThrows(Class<? extends Throwable> exception, Runnable runnable) {
        try {
            runnable.run();
            throw new AssertionError("Expected exception of type " + exception.getName() + " none was thrown");
        } catch (Throwable e) {
            if (!exception.isInstance(e)) {
                throw new AssertionError("Expected exception of type " + exception.getName() + " but got " + e.getClass().getName());
            }
        }
    }

}