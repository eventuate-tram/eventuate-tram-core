package io.eventuate.tram.redis.common;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RedisServers {

  public static class HostAndPort {
    private String host;
    private int port;

    public HostAndPort() {
    }

    public HostAndPort(String host, int port) {
      this.host = host;
      this.port = port;
    }

    public String getHost() {
      return host;
    }

    public void setHost(String host) {
      this.host = host;
    }

    public int getPort() {
      return port;
    }

    public void setPort(int port) {
      this.port = port;
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }
  }


  private List<HostAndPort> hostsAndPorts;

  public RedisServers(String redisServers) {
    if (StringUtils.isEmpty(redisServers)) {
      throw new IllegalArgumentException("redis servers are empty");
    }

    hostsAndPorts = Arrays
            .stream(redisServers.split(","))
            .map(String::trim)
            .map(this::parseHostAndPortString)
            .collect(Collectors.toList());
  }

  public List<HostAndPort> getHostsAndPorts() {
    return hostsAndPorts;
  }

  private HostAndPort parseHostAndPortString(String hostAndPortString) {
    String[] hp = hostAndPortString.split(":");

    if (hp.length != 2) {
      throw new IllegalArgumentException("Redis server should consist of host and port separated by colon");
    }

    String host = hp[0];
    int port = Integer.parseInt(hp[1]);

    return new HostAndPort(host, port);
  }
}
