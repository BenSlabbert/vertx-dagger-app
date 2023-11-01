package com.example.aspect;

public aspect AccountAspect {
    final int MIN_BALANCE = 10;
    private String test;

    public AccountAspect() {
      System.err.println("AccountAspect created");
    }

    public void setTest(String test) {
      this.test = test;
    }

    pointcut callWithdraw(int amount, Account acc) : call(boolean Account.withdraw(int)) && args(amount) && target(acc);

    before(int amount, Account acc) : callWithdraw(amount, acc) {
        System.err.println("withdraw before");
    }

    boolean around(int amount, Account acc) : callWithdraw(amount, acc) {
        System.err.println("test: " + test);
        System.err.println("withdraw around start");
        if (acc.getBalance() < amount) {
            return false;
        }
        if (acc.getBalance() - amount < MIN_BALANCE) {
            return false;
        }

        try {
            System.err.println("withdraw around before proceed");
            return proceed(amount, acc);
        } finally {
            System.err.println("withdraw around after proceed");
        }

    }

    after(int amount, Account acc) : callWithdraw(amount, acc) {
        System.err.println("withdraw after");
    }
}
