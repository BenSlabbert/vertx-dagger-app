/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.service;

import github.benslabbert.txmanager.annotation.Transactional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NestedTransactionService {

  private static final Logger log = LoggerFactory.getLogger(NestedTransactionService.class);

  @Inject
  NestedTransactionService() {}

  @Transactional
  public void runInTransaction() {
    log.info("in runInTransaction");
    nestedTransaction();
  }

  @Transactional
  private void nestedTransaction() {
    log.info("in nextedTransaction");
  }
}
