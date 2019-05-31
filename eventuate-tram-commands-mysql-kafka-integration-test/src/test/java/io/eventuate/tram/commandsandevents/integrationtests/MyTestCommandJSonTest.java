package io.eventuate.tram.commandsandevents.integrationtests;

import io.eventuate.common.json.mapper.JSonMapper;
import org.junit.Test;

public class MyTestCommandJSonTest {

  @Test
  public void shouldSerde() {
    MyTestCommand x = new MyTestCommand();
    String s = JSonMapper.toJson(x);
    MyTestCommand x2 = JSonMapper.fromJson(s, MyTestCommand.class);

  }
}
