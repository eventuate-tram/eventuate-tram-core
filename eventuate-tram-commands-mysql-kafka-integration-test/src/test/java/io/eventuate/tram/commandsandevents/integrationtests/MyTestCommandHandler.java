package io.eventuate.tram.commandsandevents.integrationtests;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.Success;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandHandlersBuilder;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.PathVariables;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;

public class MyTestCommandHandler {


  public CommandHandlers defineCommandHandlers() {
    return CommandHandlersBuilder
            .fromChannel("customerService")
            .resource("/customers/{customerId}")
            .onMessage(MyTestCommand.class, this::myHandlerMethod)
            .build();
  }

  public Message myHandlerMethod(CommandMessage<MyTestCommand> cm, PathVariables pvs) {
    System.out.println("Got command: " + cm);
    return MessageBuilder
            .withPayload(JSonMapper.toJson(new Success()))
            .build();
  }
}
