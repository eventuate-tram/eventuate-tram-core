package io.eventuate.tram.reactive.commands.consumer;

import io.eventuate.tram.commands.consumer.CommandReplyToken;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import reactor.core.publisher.Flux;

public class ReactiveCommandReplyProducer {
    private final ReactiveMessageProducer messageProducer;

    public ReactiveCommandReplyProducer(ReactiveMessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    public Flux<Message> sendReplies(CommandReplyToken commandReplyToken, Flux<Message> replies) {
        return replies
                .flatMap(reply -> {
                    Message transformedReply = MessageBuilder
                            .withMessage(reply)
                            .withExtraHeaders("", commandReplyToken.getReplyHeaders())
                            .build();

                    return messageProducer.send(commandReplyToken.getReplyChannel(), transformedReply);
                });
    }

}
