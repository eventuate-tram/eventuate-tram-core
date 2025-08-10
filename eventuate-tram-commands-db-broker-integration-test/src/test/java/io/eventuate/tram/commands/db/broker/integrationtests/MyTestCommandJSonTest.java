package io.eventuate.tram.commands.db.broker.integrationtests;

import io.eventuate.common.json.mapper.JSonMapper;
import org.junit.jupiter.api.Test;

public class MyTestCommandJSonTest {

  @Test
  public void shouldSerde() {
    MyTestCommand x = new MyTestCommand();
    String s = JSonMapper.toJson(x);
    MyTestCommand x2 = JSonMapper.fromJson(s, MyTestCommand.class);

  }
}
