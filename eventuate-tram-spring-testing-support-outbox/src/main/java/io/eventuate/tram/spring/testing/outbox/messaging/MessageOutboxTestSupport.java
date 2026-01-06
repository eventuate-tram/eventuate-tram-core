package io.eventuate.tram.spring.testing.outbox.messaging;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class MessageOutboxTestSupport {

  private final JdbcTemplate jdbcTemplate;

  public MessageOutboxTestSupport(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<Message> findMessagesSentToChannel(String channel) {
    return jdbcTemplate.query("select headers,payload from message where destination = ?", (rs, rowNum) -> {
      String headers = rs.getString("headers");
      String payload = rs.getString("payload");
      return MessageBuilder.withPayload(payload).withExtraHeaders("", JSonMapper.fromJson(headers, Map.class)).build();
    }, channel);
  }
}
