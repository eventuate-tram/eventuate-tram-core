package io.eventuate.tram.common.spring.inmemory;

import io.eventuate.tram.inmemory.EventuateTransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class EventuateSpringTransactionSynchronizationManager
        implements EventuateTransactionSynchronizationManager {

  @Override
  public boolean isTransactionActive() {
    return TransactionSynchronizationManager.isActualTransactionActive();
  }

  @Override
  public void executeAfterTransaction(Runnable callback) {
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
      @Override
      public void afterCommit() {
        callback.run();
      }
    });
  }
}
