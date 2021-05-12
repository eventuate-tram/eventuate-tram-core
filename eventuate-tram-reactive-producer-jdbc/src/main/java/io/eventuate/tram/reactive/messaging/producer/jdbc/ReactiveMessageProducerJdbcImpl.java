package io.eventuate.tram.reactive.messaging.producer.jdbc;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.reactive.jdbc.EventuateCommonReactiveJdbcOperations;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducerImplementation;
import reactor.core.publisher.Mono;

public class ReactiveMessageProducerJdbcImpl implements ReactiveMessageProducerImplementation {

  private IdGenerator idGenerator;

  private EventuateSchema eventuateSchema;
  private EventuateCommonReactiveJdbcOperations reactiveJdbcOperations;

  public ReactiveMessageProducerJdbcImpl(EventuateCommonReactiveJdbcOperations reactiveJdbcOperations,
                                         IdGenerator idGenerator,
                                         EventuateSchema eventuateSchema) {

    this.reactiveJdbcOperations = reactiveJdbcOperations;
    this.idGenerator = idGenerator;
    this.eventuateSchema = eventuateSchema;
  }

  public Mono<Message> send(Message message) {
    return reactiveJdbcOperations
            .insertIntoMessageTable(idGenerator,
              message.getPayload(),
              message.getRequiredHeader(Message.DESTINATION),
              message.getHeaders(),
              eventuateSchema)
            .map(id -> {
              message.setHeader(Message.ID, id);
              return message;
            });
  }
}
