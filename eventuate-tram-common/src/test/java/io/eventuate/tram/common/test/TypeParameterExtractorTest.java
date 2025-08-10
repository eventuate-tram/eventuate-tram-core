package io.eventuate.tram.common.test;

import io.eventuate.tram.common.TypeParameterExtractor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void shouldFailForMethodWithoutParameters() throws Exception {
      assertThrows(IllegalArgumentException.class, () -> {
        Method method = getClass().getMethod("handleCommandWithoutGenericType", String.class);
        TypeParameterExtractor.extractTypeParameter(method);
      });
    }

}