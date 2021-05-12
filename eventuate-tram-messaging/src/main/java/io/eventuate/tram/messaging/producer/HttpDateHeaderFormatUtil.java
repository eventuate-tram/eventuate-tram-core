package io.eventuate.tram.messaging.producer;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class HttpDateHeaderFormatUtil {
  public static String nowAsHttpDateString() {
    return timeAsHttpDateString(ZonedDateTime.now(ZoneId.of("GMT")));
  }

  public static String timeAsHttpDateString(ZonedDateTime gmtTime) {
    return gmtTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
  }
}
