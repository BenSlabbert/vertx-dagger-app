/* Licensed under Apache-2.0 2023. */
package com.example.payment.ioc;

import com.example.payment.repository.PaymentRepository;
import com.example.payment.service.PaymentService;
import com.example.payment.service.ServiceModule;
import com.google.protobuf.GeneratedMessageV3;
import dagger.BindsInstance;
import dagger.Component;
import io.vertx.core.buffer.Buffer;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jooq.DSLContext;

@Singleton
@Component(modules = {ServiceModule.class, Provider.EagerModule.class})
public interface TestMockPersistenceProvider extends Provider {

  PaymentService paymentService();

  @Component.Builder
  interface Builder extends BaseBuilder<Builder, TestMockPersistenceProvider> {

    @BindsInstance
    Builder paymentRepository(PaymentRepository paymentRepository);

    @BindsInstance
    Builder dslContext(DSLContext dslContext);

    @BindsInstance
    Builder dataSource(DataSource dataSource);

    @BindsInstance
    Builder kafkaConsumer(KafkaConsumer<String, Buffer> consumer);

    @BindsInstance
    Builder kafkaProducer(KafkaProducer<String, GeneratedMessageV3> producer);
  }
}
