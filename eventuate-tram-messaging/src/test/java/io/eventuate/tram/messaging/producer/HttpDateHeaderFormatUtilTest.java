package io.eventuate.tram.messaging.producer;

import org.junit.Assert;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static junit.framework.TestCase.assertEquals;

public class HttpDateHeaderFormatUtilTest {

  @Test
  public void shouldFormatDateNow() {
    Assert.assertNotNull((HttpDateHeaderFormatUtil.nowAsHttpDateString()));
  }

  @Test
  public void shouldFormatDate() {
    String expected = "Tue, 15 Nov 1994 08:12:31 GMT";
    ZonedDateTime time = ZonedDateTime.parse(expected, DateTimeFormatter.RFC_1123_DATE_TIME);
    assertEquals(expected, HttpDateHeaderFormatUtil.timeAsHttpDateString(time));
  }

}