package io.eventuate.tram.reactive.commands.consumer;


import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.DefaultCommandNameMapping;
import io.eventuate.tram.commands.consumer.*;
import io.eventuate.tram.commands.producer.CommandMessageFactory;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.tools.agent.ReactorDebugAgent;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReactiveCommandDispatcherTest {
  static {
    ReactorDebugAgent.init();
  }

  @Mock
  private ReactiveMessageConsumer messageConsumer;

  private ReactiveCommandDispatcher reactiveCommandDispatcher;

  @Mock
  private ReactiveCommandHandlers commandHandlers;
  @Mock
  private ReactiveCommandHandler commandHandler;

  private final String replyToChannel = "a-reply-to-channel";

  private final Message replyMessage = MessageBuilder.withPayload("reply-payload").build();
  @Mock
  private ReactiveMessageProducer messageProducer;

  private ReactiveCommandReplyProducer commandReplyProducer;

  @Before
  public void init() {
    when(commandHandler.getCommandClass()).thenReturn(Object.class);
    when(commandHandlers.findTargetMethod(any())).thenReturn(Optional.of(commandHandler));
    when(messageProducer.send(any(), any())).thenReturn(Mono.just(replyMessage));

    this.commandReplyProducer = new ReactiveCommandReplyProducer(messageProducer);
  }

  @Test
  public void testHandlerInvocation() {
    reactiveCommandDispatcher = new ReactiveCommandDispatcher("", commandHandlers, messageConsumer, commandReplyProducer);

    when(commandHandler.invokeMethod(any())).thenReturn(Mono.just(replyMessage));

    invokeMessageHandler();

    verify(commandHandler).invokeMethod(any());
    verify(messageProducer).send(any(), any());
  }

  @Test
  public void testAlternativeHandlerInvocation() {
    ReactiveCommandHandler alternativeCommandHandler = mock(ReactiveCommandHandler.class);

    reactiveCommandDispatcher = new ReactiveCommandDispatcher("", commandHandlers, messageConsumer, commandReplyProducer) {
      @Override
      protected Publisher<Message> invoke(ReactiveCommandHandler m, CommandMessage cm, CommandHandlerParams commandHandlerParams, CommandReplyToken commandReplyToken) {
        return alternativeCommandHandler.invokeMethod(new CommandHandlerArgs<>(cm, new PathVariables(commandHandlerParams.getPathVars()), commandReplyToken));
      }
    };

    invokeMessageHandler();

    verify(alternativeCommandHandler).invokeMethod(any());
    verify(commandHandler, never()).invokeMethod(any());
  }

  private void invokeMessageHandler() {
    Publisher<?> source = reactiveCommandDispatcher.messageHandler(makeMessage(replyToChannel));
    assertNotNull(source);

    Mono.from(source).block();
  }

  private Message makeMessage(String replyToChannel) {
    Message message = CommandMessageFactory.makeMessage(new DefaultCommandNameMapping(), "CommandChannel", null, new SomeCommand(), replyToChannel, Collections.emptyMap());
    message.setHeader(Message.ID, "123");

    return message;
  }

  @Test
  public void shouldDispatchNotification() {
    reactiveCommandDispatcher = new ReactiveCommandDispatcher("", commandHandlers, messageConsumer, commandReplyProducer);

    when(commandHandler.invokeMethod(any())).thenReturn(Mono.empty());

    Mono.from(reactiveCommandDispatcher.messageHandler(makeMessage(null))).block();

    verify(commandHandler).invokeMethod(any());
    verifyNoMoreInteractions(messageProducer);
  }

  private class SomeCommand implements Command {
  }
}
