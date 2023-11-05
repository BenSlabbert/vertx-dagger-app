/* Licensed under Apache-2.0 2023. */
package com.example.payment.ioc;

import com.example.commons.kafka.KafkaModule;
import com.example.payment.config.ConfigModule;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.repository.RepositoryModule;
import com.example.payment.service.PaymentService;
import com.example.payment.service.ServiceModule;
import com.google.protobuf.GeneratedMessageV3;
import dagger.Component;
import io.vertx.kafka.client.producer.KafkaProducer;
import java.util.Set;
import javax.inject.Singleton;
import org.jooq.DSLContext;

@Singleton
@Component(
    modules = {
      ServiceModule.class,
      ConfigModule.class,
      KafkaModule.class,
      RepositoryModule.class,
      Provider.EagerModule.class
    })
public interface TestPersistenceProvider extends Provider {

  DSLContext dslContext();

  PaymentRepository paymentRepository();

  PaymentService paymentService();

  Set<AutoCloseable> closeables();

  KafkaProducer<String, GeneratedMessageV3> producer();

  @Component.Builder
  interface Builder extends BaseBuilder<Builder, TestPersistenceProvider> {}
}
