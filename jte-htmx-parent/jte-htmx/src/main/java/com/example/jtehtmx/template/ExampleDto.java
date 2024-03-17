/* Licensed under Apache-2.0 2024. */
package com.example.jtehtmx.template;

import com.google.auto.value.AutoBuilder;

public record ExampleDto(String title, String description) {

  public static Builder builder() {
    return new AutoBuilder_ExampleDto_Builder();
  }

  @AutoBuilder
  public interface Builder {

    Builder title(String title);

    Builder description(String description);

    ExampleDto build();
  }
}
