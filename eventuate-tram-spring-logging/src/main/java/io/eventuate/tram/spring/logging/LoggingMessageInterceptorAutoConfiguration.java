package io.eventuate.tram.spring.logging;

import io.eventuate.tram.messaging.common.MessageInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;

@AutoConfiguration
public class LoggingMessageInterceptorAutoConfiguration {
    @Bean
    public MessageInterceptor messageLoggingInterceptor() {
        return new LoggingMessageInterceptor();
    }
}
