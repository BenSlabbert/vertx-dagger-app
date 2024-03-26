/* Licensed under Apache-2.0 2024. */
package com.example.iam.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.iam.IntegrationTestBase;
import com.example.iam.entity.ACL;
import com.example.iam.entity.User;
import io.vertx.core.Future;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RedisDBTest extends IntegrationTestBase {

  @Test
  void test(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    UserRepository userRepository = provider.userRepository();

    Future<Void> register =
        userRepository.register(
            "name", "password", "token", "refreshToken", "group", "role", Set.of("p-1", "p-2"));

    register
        .onComplete(testContext.succeeding(ignore -> testContext.verify(checkpoint::flag)))
        .compose(ignore -> userRepository.findByUsername("name"))
        .onComplete(
            ar ->
                testContext.verify(
                    () -> {
                      assertThat(ar.succeeded()).isTrue();
                      assertThat(ar.result())
                          .satisfies(
                              user -> {
                                User expected =
                                    new User(
                                        "name",
                                        "password",
                                        "refreshToken",
                                        new ACL("group", "role", Set.of("p-1", "p-2")));

                                assertThat(user).usingRecursiveComparison().isEqualTo(expected);
                              });
                      checkpoint.flag();
                    }));
  }
}
