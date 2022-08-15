package io.eventuate.tram.commands.consumer;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandReplyProducer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final MessageProducer messageProducer;

    public CommandReplyProducer(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    public List<Message> sendReplies(CommandReplyToken commandReplyToken, Message... replies) {
        return sendReplies(commandReplyToken, Arrays.asList(replies));
    }

    public List<Message> sendReplies(CommandReplyToken commandReplyToken, List<Message> replies) {

        if (commandReplyToken.getReplyChannel() == null) {
            if (!replies.isEmpty()) {
                throw new RuntimeException("Replies to send but not replyTo channel");
            }
            return Collections.emptyList();
        }

        if (replies.isEmpty())
            logger.trace("Null replies - not publishing");

        String replyChannel = commandReplyToken.getReplyChannel();

        List<Message> results = new ArrayList<>(replies.size());

        for (Message reply : replies) {
            Message message = MessageBuilder
                    .withMessage(reply)
                    .withExtraHeaders("", commandReplyToken.getReplyHeaders())
                    .build();
            messageProducer.send(replyChannel, message);
            results.add(message);
        }
        return results;
    }
}