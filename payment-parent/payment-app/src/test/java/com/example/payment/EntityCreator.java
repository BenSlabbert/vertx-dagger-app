/* Licensed under Apache-2.0 2023. */
package com.example.payment;

import com.example.payment.generator.entity.generated.jooq.tables.records.AccountRecord;
import com.example.payment.generator.entity.generated.jooq.tables.records.PaymentRecord;
import com.example.payment.generator.entity.generated.jooq.tables.records.SagaRecord;
import com.example.payment.generator.entity.generated.jooq.tables.records.UserProjectionRecord;
import java.util.Objects;
import org.jooq.Field;

// TODO: revisit creating this dependency graph entity creator
public class EntityCreator {

  public SagaRecord createSagaRecord() {
    return new SagaRecord();
  }

  public UserProjectionRecord createUserProjectionRecord() {
    return new UserProjectionRecord();
  }

  public PaymentRecord createPaymentRecord(AccountRecord accountRecord, SagaRecord sagaRecord) {
    PaymentRecord paymentRecord = new PaymentRecord();
    Long temp = 0L;
    Field<Long> field = null;

    temp = accountRecord.get("id", Long.class);
    Objects.requireNonNull(temp);
    field = fieldWithDependency(paymentRecord, "account_id");
    paymentRecord.set(field, temp);

    temp = sagaRecord.get("id", Long.class);
    Objects.requireNonNull(temp);
    field = fieldWithDependency(paymentRecord, "saga_id");
    paymentRecord.set(field, temp);

    return paymentRecord;
  }

  public AccountRecord createAccountRecord(UserProjectionRecord userProjectionRecord) {
    AccountRecord accountRecord = new AccountRecord();
    Long temp = 0L;
    Field<Long> field = null;

    temp = userProjectionRecord.get("id", Long.class);
    Objects.requireNonNull(temp);
    field = fieldWithDependency(accountRecord, "user_id");
    accountRecord.set(field, temp);

    return accountRecord;
  }

  private Field<Long> fieldWithDependency(org.jooq.Record recordInstance, String fieldName) {
    return (Field<Long>)
        recordInstance
            .fieldStream()
            .filter(f -> f.getName().equals(fieldName))
            .filter(f -> f.getType() == Long.class)
            .findFirst()
            .orElseThrow();
  }
}
