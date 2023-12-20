package io.eventuate.tram.spring.testing.messaging.consumer;

import com.jayway.jsonpath.JsonPath;
import io.eventuate.tram.messaging.common.Message;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertableMessage {

    private final Message message;

    public AssertableMessage(Message message) {
        this.message = message;
    }
    
    public <T> AssertableMessage payload(String path, Matcher<T> matcher) {
        T value = JsonPath.read(message.getPayload(), path);
        MatcherAssert.assertThat(value, matcher);
        return this;
    }

    public AssertableMessage header(String headerName, String expectedValue) {
        assertEquals(expectedValue, message.getRequiredHeader(headerName));
        return this;
    }
}
