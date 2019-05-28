package io.eventuate.tram.messaging.producer.common;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class HttpDateHeaderFormatUtil {
  static String nowAsHttpDateString() {
    return timeAsHttpDateString(ZonedDateTime.now(ZoneId.of("GMT")));
  }

  static String timeAsHttpDateString(ZonedDateTime gmtTime) {
    return gmtTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
  }
}
