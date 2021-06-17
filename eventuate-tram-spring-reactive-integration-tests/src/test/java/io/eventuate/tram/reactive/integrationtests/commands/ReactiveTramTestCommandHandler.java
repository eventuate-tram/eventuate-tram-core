package io.eventuate.tram.reactive.integrationtests.commands;

import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlers;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlersBuilder;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlerReplyBuilder.withSuccess;

public class ReactiveTramTestCommandHandler {

  private String commandChannel;
  private BlockingQueue<TestCommand> commandQueue = new LinkedBlockingDeque<>();

  public ReactiveTramTestCommandHandler(String commandChannel) {
    this.commandChannel = commandChannel;
  }

  public String getCommandChannel() {
    return commandChannel;
  }

  public BlockingQueue<TestCommand> getCommandQueue() {
    return commandQueue;
  }

  public ReactiveCommandHandlers getCommandHandlers() {
    return ReactiveCommandHandlersBuilder
            .fromChannel(commandChannel)
            .onMessage(TestCommand.class, this::handleTestCommand)
            .build();
  }

  public Publisher<Message> handleTestCommand(CommandMessage<TestCommand> commandMessage) {
    return Mono
            .defer(() -> Mono.just(commandQueue.add(commandMessage.getCommand())))
            .then(withSuccess());
  }
}
