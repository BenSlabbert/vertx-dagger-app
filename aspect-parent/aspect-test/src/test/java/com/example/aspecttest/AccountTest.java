/* Licensed under Apache-2.0 2023. */
package com.example.aspecttest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.aspect.Account;
import com.example.aspect.AccountAspect;
import org.aspectj.lang.Aspects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountTest {

  private Account account;

  @BeforeEach
  public void before() {
    // this creates a singleton instance of AccountAspect
    // now we can add some setters to provide dependencies
    AccountAspect aa = Aspects.aspectOf(AccountAspect.class);
    aa.setTest("test incoming!");
    account = new Account(20);
  }

  @Test
  void success() {
    assertTrue(account.withdraw(5));
  }

  @Test
  void fail() {
    assertFalse(account.withdraw(11));
  }
}
