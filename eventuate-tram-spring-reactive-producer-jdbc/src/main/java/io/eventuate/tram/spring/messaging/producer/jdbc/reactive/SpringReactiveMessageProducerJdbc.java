package io.eventuate.tram.spring.messaging.producer.jdbc.reactive;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.spring.jdbc.reactive.EventuateCommonReactiveSpringJdbcOperations;
import io.eventuate.tram.messaging.common.Message;
import reactor.core.publisher.Mono;

public class SpringReactiveMessageProducerJdbc {

  private IdGenerator idGenerator;

  private EventuateSchema eventuateSchema;
  private EventuateCommonReactiveSpringJdbcOperations eventuateCommonReactiveSpringJdbcOperations;

  public SpringReactiveMessageProducerJdbc(EventuateCommonReactiveSpringJdbcOperations eventuateCommonReactiveSpringJdbcOperations,
                                           IdGenerator idGenerator,
                                           EventuateSchema eventuateSchema) {

    this.eventuateCommonReactiveSpringJdbcOperations = eventuateCommonReactiveSpringJdbcOperations;
    this.idGenerator = idGenerator;
    this.eventuateSchema = eventuateSchema;
  }

  public Mono<Message> send(Message message) {
    return eventuateCommonReactiveSpringJdbcOperations
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
