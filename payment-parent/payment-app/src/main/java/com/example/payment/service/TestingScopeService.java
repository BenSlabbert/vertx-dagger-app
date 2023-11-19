/* Licensed under Apache-2.0 2023. */
package com.example.payment.service;

import com.example.payment.scope.TransactionComponent;
import com.example.payment.scope.TransactionManager;
import com.example.payment.scope.repo.Repo;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class TestingScopeService {

  private static final Logger log = LoggerFactory.getLogger(TestingScopeService.class);

  private final Provider<TransactionComponent.Builder> transactionComponentProvider;

  @Inject
  TestingScopeService(Provider<TransactionComponent.Builder> transactionComponent) {
    this.transactionComponentProvider = transactionComponent;
  }

  public void handle() {
    TransactionComponent.Builder builder = transactionComponentProvider.get();
    TransactionComponent tc1 = builder.build();
    TransactionComponent tc2 = builder.build();

    TransactionManager t1 = tc1.transactionManager();
    TransactionManager t2 = tc2.transactionManager();

    t1.startTransaction();
    t2.startTransaction();

    Repo repo1 = tc1.repo();
    Repo repo2 = tc2.repo();

    Long name1 = repo1.save("name_1");
    log.info("name1: " + name1);
    Long name2 = repo2.save("name_2");
    log.info("name2: " + name2);

    Repo.Projection fetch1 = repo1.fetch(name2);
    log.info("should not find fetch1: " + fetch1);
    Repo.Projection fetch2 = repo2.fetch(name1);
    log.info("should not find fetch2: " + fetch2);

    repo1.fetchAll().forEach(payment -> log.info("repo1#fetchAll: " + payment.id()));
    repo2.fetchAll().forEach(payment -> log.info("repo2#fetchAll: " + payment.id()));

    t1.commit();
    t2.commit();

    t1.close();
    t2.close();
  }
}
