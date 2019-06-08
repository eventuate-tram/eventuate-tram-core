package io.eventuate.tram.springcloudsleuthintegration.test;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class TestController {

  @Autowired
  private MessageProducer messageProducer;

  @PostMapping(path= "/foo/{id}")
  public String sendSomething(@RequestBody TestMessage message, @PathVariable String id) {
    Message message1 = MessageBuilder.withPayload(JSonMapper.toJson(message)).build();
    messageProducer.send("testChannel", message1);
    return message1.getId();
  }

  @PostMapping(path= "/bar")
  public String sendSomethingElse(@RequestBody String message) {
    return message;
  }


}
