package io.eventuate.tram.common.test;

import io.eventuate.tram.common.TypeParameterExtractor;
import org.junit.Test;

import java.lang.reflect.Method;

import static junit.framework.TestCase.assertEquals;

public class TypeParameterExtractorTest {

    public static class CommandMessage<T> {
    }

    public static class ReserveCreditCommand {
    }

    public void handleCommand(CommandMessage<ReserveCreditCommand> cm) {
    }

    public void handleCommandWithoutGenericType(String notACommand) {
    }

    @Test
    public void shouldExtractTypeParameter() throws Exception {
        Method method = getClass().getMethod("handleCommand", CommandMessage.class);
        Class<?> commandClass = TypeParameterExtractor.extractTypeParameter(method);
        assertEquals(ReserveCreditCommand.class, commandClass);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailForMethodWithoutParameters() throws Exception {
        Method method = getClass().getMethod("handleCommandWithoutGenericType", String.class);
        TypeParameterExtractor.extractTypeParameter(method);
    }

}