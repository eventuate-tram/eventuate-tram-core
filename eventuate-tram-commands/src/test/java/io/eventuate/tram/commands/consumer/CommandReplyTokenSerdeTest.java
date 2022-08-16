package io.eventuate.tram.commands.consumer;

import io.eventuate.common.json.mapper.JSonMapper;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class CommandReplyTokenSerdeTest {

    @Test
    public void shouldSerializeAndDeserialize() {
        CommandReplyToken crt = new CommandReplyToken(Collections.singletonMap("x", "y"), "Hollywood");
        String s = JSonMapper.toJson(crt);
        CommandReplyToken newCrt = JSonMapper.fromJson(s, CommandReplyToken.class);
        assertEquals(crt.getReplyChannel(), newCrt.getReplyChannel());
        assertEquals(crt.getReplyHeaders(), newCrt.getReplyHeaders());
    }
}