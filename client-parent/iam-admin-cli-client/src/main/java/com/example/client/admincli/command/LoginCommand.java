/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.command;

import com.example.client.admincli.auth.AuthClientFactory;
import com.example.client.admincli.util.DisplayErrorUtil;
import com.example.client.admincli.util.KeyFile;
import com.example.client.admincli.util.KeyFileUtil;
import com.example.commons.future.FutureUtil;
import com.example.iam.auth.api.dto.LoginRequestDto;
import com.example.iam.auth.api.dto.LoginResponseDto;
import com.example.starter.iam.auth.client.IamAuthClient;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Singleton
@Command(name = "login", description = "create a user in the IAM server")
public class LoginCommand implements Callable<Integer> {

  private final IamAuthClient iamAuthClient;
  private final PrintStream out;
  private final Vertx vertx;

  @Inject
  LoginCommand(@Named("out") PrintStream out, AuthClientFactory authClientProvider, Vertx vertx) {
    this.iamAuthClient = authClientProvider.provide();
    this.out = out;
    this.vertx = vertx;
  }

  @Option(
      required = true,
      names = {"-u", "--username"},
      description = "user's username")
  private String username;

  @Option(
      required = true,
      names = {"-p", "--password"},
      description = "user's password")
  private String password;

  @Option(
      required = false,
      names = {"-o", "--output"},
      description = "output file path")
  private Path output;

  @Override
  public Integer call() {
    out.println("execute create");
    out.println("output: " + output);

    Future<LoginResponseDto> resp =
        FutureUtil.runFutureSync(
            iamAuthClient.login(
                LoginRequestDto.builder().username(username).password(password).build()));

    if (resp.failed()) {
      return DisplayErrorUtil.handleFailure(out, resp);
    }

    LoginResponseDto result = resp.result();

    if (null != output) {
      out.println("writing credentials to: " + output);
      KeyFileUtil.write(
          vertx,
          output,
          KeyFile.builder()
              .username(username)
              .password(password.toCharArray())
              .token(result.token())
              .refreshToken(result.refreshToken())
              .build());
    } else {
      out.println("result: " + result);
    }

    return result != null ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
  }
}
