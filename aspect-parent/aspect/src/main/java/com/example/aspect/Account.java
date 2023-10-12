/* Licensed under Apache-2.0 2023. */
package com.example.aspect;

public class Account {

  private int balance;

  public Account(int balance) {
    this.balance = balance;
  }

  public boolean withdraw(int amount) {
    System.err.println("withdraw inside");

    if (balance < amount) {
      return false;
    }
    balance = balance - amount;
    return true;
  }

  public int getBalance() {
    return balance;
  }
}
