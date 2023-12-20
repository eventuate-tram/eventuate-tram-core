package io.eventuate.tram.spring.testing.events.publisher;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.events.common.EventMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;


public class EventOutboxTestSupport {

    private final JdbcTemplate jdbcTemplate;

    public EventOutboxTestSupport(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void assertEventPublished(String channel, String aggregateId, String eventType) {

        List<Message> messages = jdbcTemplate.query("select headers,payload from message where destination = ?", (rs, rowNum) -> {
            String headers = rs.getString("headers");
            String payload = rs.getString("payload");
            return MessageBuilder.withPayload(payload).withExtraHeaders("", JSonMapper.fromJson(headers, Map.class)).build();
        }, channel).stream().filter(m -> m.getHeader(EventMessageHeaders.AGGREGATE_ID).map(s -> s.equals(aggregateId)).orElse(false)).collect(java.util.stream.Collectors.toList());

        assertThat(messages, hasSize(1));
        Message message = messages.get(0);
        assertThat(message.getRequiredHeader(EventMessageHeaders.EVENT_TYPE), equalTo(eventType));

    }

}
