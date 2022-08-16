package io.eventuate.tram.commands.consumer;

import java.util.Map;

public class CommandReplyToken {
    private Map<String, String> replyHeaders;
    private String replyChannel;

    public CommandReplyToken(Map<String, String> correlationHeaders, String replyChannel) {
        this.replyHeaders = correlationHeaders;
        this.replyChannel = replyChannel;
    }

    private CommandReplyToken() {
        // For ObjectMapper
    }

    public Map<String, String> getReplyHeaders() {
        return replyHeaders;
    }

    public String getReplyChannel() {
        return replyChannel;
    }

    public void setReplyHeaders(Map<String, String> replyHeaders) {
        this.replyHeaders = replyHeaders;
    }

    public void setReplyChannel(String replyChannel) {
        this.replyChannel = replyChannel;
    }
}
