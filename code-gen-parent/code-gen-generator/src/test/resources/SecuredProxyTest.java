/* Licensed under Apache-2.0 2024. */
package my.test;

import com.example.codegen.annotation.security.SecuredProxy;

@SecuredProxy
public interface SecuredProxyTest {

  @SecuredProxy.SecuredAction(
      group = "group",
      role = "role",
      permissions = {"p1"})
  void testA();

  @SecuredProxy.SecuredAction(
      group = "group",
      role = "role",
      permissions = {"p1", "p2"})
  void testB();
}
