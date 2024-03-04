package io.eventuate.tram.spring.testing.messaging.consumer;

import com.jayway.jsonpath.JsonPath;
import io.eventuate.tram.messaging.common.Message;
import org.hamcrest.Matcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertableMessage {

    private final Message message;

    public AssertableMessage(Message message) {
        this.message = message;
    }
    
    public <T> AssertableMessage payload(String path, Matcher<T> matcher) {
        T value = JsonPath.read(message.getPayload(), path);
        assertThat(value, matcher);
        return this;
    }

    public AssertableMessage payload(Matcher<String> matcher) {
        assertThat(message.getPayload(), matcher);
        return this;
    }

    public AssertableMessage header(String headerName, String expectedValue) {
        assertEquals(expectedValue, message.getRequiredHeader(headerName));
        return this;
    }
}
