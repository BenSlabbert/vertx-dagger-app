/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.command;

import com.example.client.admincli.auth.AuthClientFactory;
import com.example.client.admincli.util.DisplayErrorUtil;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.Access;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.RegisterRequestDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.RegisterResponseDto;
import github.benslabbert.vertxdaggercommons.future.FutureUtil;
import github.benslabbert.vertxdaggerstarter.iamauthclient.IamAuthClient;
import io.vertx.core.Future;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Singleton
@Command(name = "register", description = "register a user in the IAM server")
public class RegisterCommand implements Callable<Integer> {

  private final IamAuthClient iamAuthClient;
  private final PrintStream out;

  @Inject
  RegisterCommand(@Named("out") PrintStream out, AuthClientFactory authClientProvider) {
    this.iamAuthClient = authClientProvider.provide();
    this.out = out;
  }

  @Option(
      required = true,
      names = {"-u", "--username"},
      description = "new user's username")
  private String username;

  @Option(
      required = true,
      names = {"-p", "--password"},
      description = "new user's password")
  private String password;

  private enum Role {
    ADMIN,
    DELIVERY_TRUCK,
    UI_USER
  }

  @Option(
      required = true,
      names = {"-r", "--role"},
      description = {"Specify role for new user (case in-sensitive) (${COMPLETION-CANDIDATES})"})
  private Role role;

  @Override
  public Integer call() {
    out.println("execute register");

    Access access =
        switch (role) {
          case ADMIN -> Access.builder().group("root").role("admin").addPermission("*").build();
          case DELIVERY_TRUCK ->
              Access.builder()
                  .group("system")
                  .role("delivery-truck-api")
                  .addPermission("delivery-truck-api:r")
                  .addPermission("delivery-truck-api:w")
                  .build();
          case UI_USER ->
              Access.builder()
                  .group("user")
                  .role("ui-user")
                  .addPermission("ui-user:r")
                  .addPermission("ui-user:w")
                  .build();
        };

    Future<RegisterResponseDto> resp =
        FutureUtil.runFutureSync(
            iamAuthClient.register(
                RegisterRequestDto.builder()
                    .username(username)
                    .password(password)
                    .access(access)
                    .build()));

    if (resp.failed()) {
      return DisplayErrorUtil.handleFailure(out, resp);
    }

    RegisterResponseDto result = resp.result();
    out.println("result: " + result);

    return result != null ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
  }
}
