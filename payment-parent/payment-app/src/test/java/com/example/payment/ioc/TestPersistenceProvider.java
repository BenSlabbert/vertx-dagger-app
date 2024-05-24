/* Licensed under Apache-2.0 2023. */
package com.example.payment.ioc;

import com.example.payment.repository.PaymentRepository;
import com.example.payment.repository.RepositoryModule;
import com.example.payment.scope.TransactionComponent;
import com.example.payment.scope.TransactionModule;
import com.example.payment.service.PaymentService;
import com.example.payment.service.ServiceModule;
import com.example.starter.jdbc.pool.JdbcPoolModule;
import dagger.Component;
import github.benslabbert.vertxdaggercommons.jooq.DataSourceDslContextModule;
import java.util.Set;
import javax.inject.Singleton;
import org.jooq.DSLContext;

@Singleton
@Component(
    modules = {
      ServiceModule.class,
      DataSourceDslContextModule.class,
      JdbcPoolModule.class,
      RepositoryModule.class,
      Provider.EagerModule.class,
      TransactionModule.class
    })
public interface TestPersistenceProvider extends Provider {

  DSLContext dslContext();

  PaymentRepository paymentRepository();

  PaymentService paymentService();

  Set<AutoCloseable> closeables();

  TransactionComponent.Builder transactionComponentBuilder();

  @Component.Builder
  interface Builder extends BaseBuilder<Builder, TestPersistenceProvider> {}
}
