/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.jdbc.TestBase;
import org.junit.jupiter.api.Test;

class JdbcServiceTest extends TestBase {

  @Test
  void test() {
    JdbcService jdbcService = provider.jdbcService();
    assertThat(jdbcService).isNotNull();

    jdbcService.runBatchInsert(4);
    jdbcService.runSelect();
    jdbcService.forEach(System.err::println);

    try (var s = jdbcService.stream()) {
      assertThat(s.toList())
          .satisfiesExactly(
              id -> assertThat(id).isEqualTo(1L),
              id -> assertThat(id).isEqualTo(2L),
              id -> assertThat(id).isEqualTo(3L),
              id -> assertThat(id).isEqualTo(4L));
    }
  }
}
