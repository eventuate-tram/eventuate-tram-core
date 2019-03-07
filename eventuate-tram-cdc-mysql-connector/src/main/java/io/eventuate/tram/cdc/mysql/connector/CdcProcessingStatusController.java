package io.eventuate.tram.cdc.mysql.connector;

import io.eventuate.local.common.CdcProcessingStatus;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CdcProcessingStatusController {
  private BinlogEntryReaderProvider binlogEntryReaderProvider;

  public CdcProcessingStatusController(BinlogEntryReaderProvider binlogEntryReaderProvider) {
    this.binlogEntryReaderProvider = binlogEntryReaderProvider;
  }

  @RequestMapping(value = "/cdc-event-processing-status", method = RequestMethod.GET)
  public CdcProcessingStatus allCdcEventsProcessed(@RequestParam("readerName") String readerName) {
    return binlogEntryReaderProvider.getReader(readerName).getCdcProcessingStatusService().getCurrentStatus();
  }
}