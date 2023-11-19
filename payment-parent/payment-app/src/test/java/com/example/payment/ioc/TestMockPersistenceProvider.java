/* Licensed under Apache-2.0 2023. */
package com.example.payment.ioc;

import com.example.payment.repository.PaymentRepository;
import com.example.payment.scope.TransactionComponent;
import com.example.payment.scope.TransactionModule;
import com.example.payment.service.PaymentService;
import com.example.payment.service.ServiceModule;
import dagger.BindsInstance;
import dagger.Component;
import java.util.Set;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jooq.DSLContext;

@Singleton
@Component(modules = {ServiceModule.class, Provider.EagerModule.class, TransactionModule.class})
public interface TestMockPersistenceProvider extends Provider {

  PaymentService paymentService();

  TransactionComponent.Builder transactionComponentBuilder();

  @Component.Builder
  interface Builder extends BaseBuilder<Builder, TestMockPersistenceProvider> {

    @BindsInstance
    Builder paymentRepository(PaymentRepository paymentRepository);

    @BindsInstance
    Builder dslContext(DSLContext dslContext);

    @BindsInstance
    Builder dataSource(DataSource dataSource);

    @BindsInstance
    Builder closeables(Set<AutoCloseable> closeables);
  }
}
