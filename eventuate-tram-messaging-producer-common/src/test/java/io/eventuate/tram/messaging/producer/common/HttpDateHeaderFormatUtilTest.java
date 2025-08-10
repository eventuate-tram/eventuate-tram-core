package io.eventuate.tram.messaging.producer.common;

import io.eventuate.tram.messaging.producer.HttpDateHeaderFormatUtil;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpDateHeaderFormatUtilTest {

  @Test
  public void shouldFormatDateNow() {
    assertNotNull((HttpDateHeaderFormatUtil.nowAsHttpDateString()));
  }

  @Test
  public void shouldFormatDate() {
    String expected = "Tue, 15 Nov 1994 08:12:31 GMT";
    ZonedDateTime time = ZonedDateTime.parse(expected, DateTimeFormatter.RFC_1123_DATE_TIME);
    assertEquals(expected, HttpDateHeaderFormatUtil.timeAsHttpDateString(time));
  }

}