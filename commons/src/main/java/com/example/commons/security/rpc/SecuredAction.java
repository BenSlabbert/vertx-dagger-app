/* Licensed under Apache-2.0 2024. */
package com.example.commons.security.rpc;

import com.google.auto.value.AutoBuilder;
import java.util.List;

public record SecuredAction(String group, String role, List<String> permissions) {

  public static Builder builder() {
    return new AutoBuilder_SecuredAction_Builder();
  }

  @AutoBuilder
  public interface Builder {
    Builder group(String group);

    Builder role(String role);

    Builder permissions(List<String> permissions);

    SecuredAction build();
  }
}
