package io.eventuate.tram.reactive.commands.consumer;


import io.eventuate.tram.commands.consumer.CommandHandlerParams;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReactiveCommandDispatcherTest {

  private ReactiveMessageConsumer messageConsumer = mock(ReactiveMessageConsumer.class);
  private ReactiveMessageProducer messageProducer = mock(ReactiveMessageProducer.class);

  private ReactiveCommandDispatcher reactiveCommandDispatcher;

  private ReactiveCommandHandlers commandHandlers;
  private ReactiveCommandHandler commandHandler;

  @Before
  public void init() {
    commandHandler = mock(ReactiveCommandHandler.class);
    when(commandHandler.getCommandClass()).thenReturn(Object.class);
    commandHandlers = mock(ReactiveCommandHandlers.class);
    when(commandHandlers.findTargetMethod(any())).thenReturn(Optional.of(commandHandler));
  }

  @Test
  public void testHandlerInvocation() {
    reactiveCommandDispatcher = new ReactiveCommandDispatcher("", commandHandlers, messageConsumer, messageProducer);

    invokeMessageHandler();

    verify(commandHandler).invokeMethod(any(), any());
  }

  @Test
  public void testAlternativeHandlerInvocation() {
    ReactiveCommandHandler alternativeCommandHandler = mock(ReactiveCommandHandler.class);

    reactiveCommandDispatcher = new ReactiveCommandDispatcher("", commandHandlers, messageConsumer, messageProducer) {
      @Override
      protected Publisher<List<Message>> invoke(ReactiveCommandHandler m, CommandMessage cm, CommandHandlerParams commandHandlerParams) {
        return alternativeCommandHandler.invokeMethod(cm, commandHandlerParams.getPathVars());
      }
    };

    invokeMessageHandler();

    verify(alternativeCommandHandler).invokeMethod(any(), any());
    verify(commandHandler, never()).invokeMethod(any(), any());
  }

  private void invokeMessageHandler() {
    reactiveCommandDispatcher.messageHandler(MessageBuilder.withPayload("{}").withHeader("ID", "id").build());
  }
}
