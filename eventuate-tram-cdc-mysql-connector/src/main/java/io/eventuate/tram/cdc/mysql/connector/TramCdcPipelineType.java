package io.eventuate.tram.cdc.mysql.connector;

public enum TramCdcPipelineType {
  MYSQL_BINLOG("eventuate-tram-mysql-binlog"),
  EVENT_POLLING("eventuate-tram-event-polling"),
  POSTGRES_WAL("eventuate-tram-postgres-wal");

  public String stringRepresentation;

  TramCdcPipelineType(String stringRepresentation) {
    this.stringRepresentation = stringRepresentation;
  }
}
