/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.command;

import com.example.client.admincli.auth.AuthClientFactory;
import com.example.client.admincli.util.DisplayErrorUtil;
import com.example.commons.future.FutureUtil;
import com.example.iam.auth.api.dto.RegisterRequestDto;
import com.example.iam.auth.api.dto.RegisterResponseDto;
import com.example.iam.auth.api.perms.Access;
import com.example.iam.auth.api.perms.AdminAccessProvider;
import com.example.iam.auth.api.perms.DeliveryTruckAccessProvider;
import com.example.iam.auth.api.perms.UserAccessProvider;
import com.example.starter.iam.auth.client.IamAuthClient;
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

  enum Role {
    ADMIN,
    DELIVERY_TRUCK,
    UI_USER
  }

  @Option(
      required = true,
      names = {"-r", "--role"},
      description = {"Specify role for new user (case in-sensitive) (${COMPLETION-CANDIDATES})"})
  Role role;

  @Override
  public Integer call() {
    out.println("execute register");

    Access access =
        switch (role) {
          case ADMIN -> AdminAccessProvider.createAccess();
          case DELIVERY_TRUCK -> DeliveryTruckAccessProvider.createAccess();
          case UI_USER -> UserAccessProvider.createAccess();
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
