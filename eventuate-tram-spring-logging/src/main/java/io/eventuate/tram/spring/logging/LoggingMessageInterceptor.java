package io.eventuate.tram.spring.logging;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingMessageInterceptor implements MessageInterceptor {

    private static final Logger logger = LoggerFactory.getLogger("io.eventuate.activity");

    @Override
    public void postSend(Message message, Exception e) {
        logger.info("Message Sent: {}", message);
    }

    @Override
    public void preHandle(String subscriberId, Message message) {
        logger.info("message received: {} {}", subscriberId, message);
    }
}
