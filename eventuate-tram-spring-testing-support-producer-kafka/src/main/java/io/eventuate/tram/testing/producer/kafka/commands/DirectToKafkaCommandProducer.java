package io.eventuate.tram.testing.producer.kafka.commands;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.messaging.kafka.producer.EventuateKafkaProducer;
import io.eventuate.messaging.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.DefaultCommandNameMapping;
import io.eventuate.tram.commands.producer.CommandProducerImpl;
import io.eventuate.tram.messaging.common.Message;

import java.util.Map;
import java.util.UUID;

public class DirectToKafkaCommandProducer {

  private final CommandProducerImpl commandProducer;

  public DirectToKafkaCommandProducer(String bootstrapServer) {
    var eventuateKafkaProducer = new EventuateKafkaProducer(bootstrapServer, EventuateKafkaProducerConfigurationProperties.empty());
    this.commandProducer= new CommandProducerImpl((destination, message) -> {
      message.getHeaders().put(Message.ID, UUID.randomUUID().toString());
      eventuateKafkaProducer.send(destination, "1", JSonMapper.toJson(message));
    }, new DefaultCommandNameMapping());
  }

  public String send(String channel, Command command, String replyTo, Map<String, String> headers) {
    return commandProducer.send(channel, command, replyTo, headers);
  }
}
