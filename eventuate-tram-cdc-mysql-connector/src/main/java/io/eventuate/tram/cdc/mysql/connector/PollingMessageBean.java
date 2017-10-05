package io.eventuate.tram.cdc.mysql.connector;

public class PollingMessageBean {
  private String id;
  private String destination;
  private String payload;
  private String headers;

  public PollingMessageBean() {
  }

  public PollingMessageBean(String id, String destination, String payload, String headers) {
    this.id = id;
    this.destination = destination;
    this.payload = payload;
    this.headers = headers;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public String getHeaders() {
    return headers;
  }

  public void setHeaders(String headers) {
    this.headers = headers;
  }
}
