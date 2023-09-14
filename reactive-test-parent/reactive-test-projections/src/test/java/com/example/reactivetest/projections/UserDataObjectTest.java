/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.projections;

import static com.example.reactivetest.generator.entity.generated.jooq.tables.UserData.USER_DATA;
import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.sqlclient.templates.annotations.Column;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;

class UserDataObjectTest {

  @Test
  void test() {
    ReflectionUtils.streamFields(
            UserDataObject.class,
            field ->
                Arrays.stream(field.getDeclaredAnnotations())
                    .anyMatch(a -> a.annotationType().equals(Column.class)),
            ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
        .forEach(
            field ->
                assertThat(Arrays.asList(field.getDeclaredAnnotationsByType(Column.class)))
                    .singleElement()
                    .satisfies(
                        column -> {
                          String name = column.name();
                          assertThat(USER_DATA.fields(name))
                              .singleElement()
                              .satisfies(f -> assertThat(f.getName()).isEqualTo(name));
                        }));
  }
}
