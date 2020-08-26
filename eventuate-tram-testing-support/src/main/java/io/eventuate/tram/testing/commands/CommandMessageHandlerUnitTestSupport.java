package io.eventuate.tram.testing.commands;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.commands.producer.CommandProducerImpl;
import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.util.SimpleIdGenerator;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class CommandMessageHandlerUnitTestSupport {
  private MessageHandler handler;
  private CommandDispatcher dispatcher;
  private SimpleIdGenerator idGenerator = new SimpleIdGenerator();

  private String replyDestination;
  private Message replyMessage;
  private CommandProducer producer;

  public static CommandMessageHandlerUnitTestSupport given() {
    return new CommandMessageHandlerUnitTestSupport();
  }

  public CommandMessageHandlerUnitTestSupport commandHandlers(CommandHandlers commandHandlers) {
    this.dispatcher = new CommandDispatcher("mockCommandDispatcher-" + System.currentTimeMillis(),
            commandHandlers,
            new MessageConsumer() {
              @Override
              public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
                CommandMessageHandlerUnitTestSupport.this.handler = handler;
                return () -> {};
              }

              @Override
              public String getId() {
                return null;
              }

              @Override
              public void close() {

              }
            },
            (destination, message) -> {
              CommandMessageHandlerUnitTestSupport.this.replyDestination = destination;
              CommandMessageHandlerUnitTestSupport.this.replyMessage = message;
            });

    dispatcher.initialize();
    producer = new CommandProducerImpl((destination, message) -> {
      String id = idGenerator.generateId().toString();
      message.getHeaders().put(Message.ID, id);
      dispatcher.messageHandler(message);
    });

    return this;
  }

  public CommandMessageHandlerUnitTestSupport when() {
    return this;
  }

  public CommandMessageHandlerUnitTestSupport then() {
    return this;
  }


  public CommandMessageHandlerUnitTestSupport receives(Command command) {
    producer.send("don't care", command, "reply_to", Collections.emptyMap());
    return this;
  }

  public CommandMessageHandlerUnitTestSupport verify(Consumer<Message> c) {
    return verifyReply(c);
  }

  public CommandMessageHandlerUnitTestSupport verifyReply(Consumer<Message> c) {
    c.accept(replyMessage);
    return this;
  }

  public static void assertReplyTypeEquals(Class<?> replyType, Message reply) {
    assertEquals(replyType.getName(), reply.getRequiredHeader(ReplyMessageHeaders.REPLY_TYPE));
  }

  public <CH, CT extends Command, RT> CommandMessageHandlerUnitTestSupport expectCommandHandlerInvoked(CH commandHandlers, BiConsumer<CH, CommandMessage<CT>> c, BiConsumer<CommandMessage<CT>, CommandHandlerReply<RT>> consumer) {
    ArgumentCaptor<CommandMessage<CT>> arg = ArgumentCaptor.forClass(CommandMessage.class);
    c.accept(Mockito.verify(commandHandlers), arg.capture());
    consumer.accept(arg.getValue(), makeCommandHandlerReply(replyMessage));
    return this;
  }

  private <RT> CommandHandlerReply<RT> makeCommandHandlerReply(Message replyMessage) {
    return new CommandHandlerReply<>(JSonMapper.fromJsonByName(replyMessage.getPayload(), replyMessage.getRequiredHeader(ReplyMessageHeaders.REPLY_TYPE)), replyMessage);
  }


}
