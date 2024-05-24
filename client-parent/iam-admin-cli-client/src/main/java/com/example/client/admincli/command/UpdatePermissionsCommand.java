/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.command;

import com.example.client.admincli.auth.AuthClientFactory;
import com.example.client.admincli.util.DisplayErrorUtil;
import com.example.iam.auth.api.dto.UpdatePermissionsRequestDto;
import com.example.iam.auth.api.dto.UpdatePermissionsResponseDto;
import com.example.iam.auth.api.perms.Access;
import com.example.starter.iam.auth.client.IamAuthClient;
import github.benslabbert.vertxdaggercommons.future.FutureUtil;
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
@Command(name = "update-permissions", description = "register a user in the IAM server")
public class UpdatePermissionsCommand implements Callable<Integer> {

  private final IamAuthClient iamAuthClient;
  private final PrintStream out;

  @Inject
  UpdatePermissionsCommand(@Named("out") PrintStream out, AuthClientFactory authClientProvider) {
    this.iamAuthClient = authClientProvider.provide();
    this.out = out;
  }

  @Option(
      required = true,
      names = {"-u", "--username"},
      description = "user's username")
  private String username;

  @Option(
      required = true,
      names = {"-g", "--group"},
      description = "group permission")
  private String group;

  @Option(
      required = true,
      names = {"-r", "--role"},
      description = "role permission")
  private String role;

  @Option(
      required = true,
      names = {"-p", "--permissions"},
      description = "permissions")
  private String[] permissions;

  @Override
  public Integer call() {
    out.println("execute update-permissions");

    UpdatePermissionsRequestDto req =
        UpdatePermissionsRequestDto.builder()
            .username(username)
            .access(Access.builder().group(group).role(role).addPermissions(permissions).build())
            .build();

    Future<UpdatePermissionsResponseDto> resp =
        FutureUtil.runFutureSync(iamAuthClient.updatePermissions(req));
    if (resp.failed()) {
      return DisplayErrorUtil.handleFailure(out, resp);
    }

    UpdatePermissionsResponseDto result = resp.result();
    out.println("result: " + result);

    return result != null ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
  }
}
