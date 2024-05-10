/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.service;

import github.benslabbert.txmanager.annotation.Transactional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Transactional
public class TransactionService {

  private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

  @Inject
  TransactionService() {}

  @Transactional
  public void test() {}
}
