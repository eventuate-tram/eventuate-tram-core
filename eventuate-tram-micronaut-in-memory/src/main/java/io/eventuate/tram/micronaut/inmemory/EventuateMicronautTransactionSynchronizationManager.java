package io.eventuate.tram.micronaut.inmemory;

import io.eventuate.tram.inmemory.EventuateTransactionSynchronizationManager;
import io.micronaut.transaction.support.TransactionSynchronizationAdapter;
import io.micronaut.transaction.support.TransactionSynchronizationManager;

public class EventuateMicronautTransactionSynchronizationManager
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
