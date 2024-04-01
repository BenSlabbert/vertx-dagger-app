/* Licensed under Apache-2.0 2024. */
package com.example.codegen.security;

import com.example.codegen.annotation.security.SecuredProxy;

@SecuredProxy
public interface SecuredRpc {

  @SecuredProxy.SecuredAction(
      group = "group",
      role = "role",
      permissions = {"p1", "p2"})
  void action1();

  @SecuredProxy.SecuredAction(
      group = "group",
      role = "role",
      permissions = {"p3"})
  void action2();

  @SecuredProxy.SecuredAction(group = "group", role = "role")
  void action3();
}
