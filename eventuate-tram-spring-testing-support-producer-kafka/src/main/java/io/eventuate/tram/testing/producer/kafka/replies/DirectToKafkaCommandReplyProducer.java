package io.eventuate.tram.testing.producer.kafka.replies;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.messaging.kafka.producer.EventuateKafkaProducer;
import io.eventuate.messaging.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import io.eventuate.tram.commands.consumer.CommandReplyProducer;
import io.eventuate.tram.commands.consumer.CommandReplyToken;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageHeaderUtils;

import java.util.List;

public class DirectToKafkaCommandReplyProducer {

  private final CommandReplyProducer commandReplyProducer;

  public DirectToKafkaCommandReplyProducer(String bootstrapServer) {
    var eventuateKafkaProducer = new EventuateKafkaProducer(bootstrapServer, EventuateKafkaProducerConfigurationProperties.empty());
    this.commandReplyProducer = new CommandReplyProducer((destination, message) -> {
      MessageHeaderUtils.prepareMessageHeaders(destination, message);
      eventuateKafkaProducer.send(destination, "1", JSonMapper.toJson(message));
    });
  }

  public List<Message> sendReplies(CommandReplyToken commandReplyToken, Message... replies) {
    return commandReplyProducer.sendReplies(commandReplyToken, replies);
  }

  public List<Message> sendReplies(CommandReplyToken commandReplyToken, List<Message> replies) {
    return commandReplyProducer.sendReplies(commandReplyToken, replies);
  }
}
