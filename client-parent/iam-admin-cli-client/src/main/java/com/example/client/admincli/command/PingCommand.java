/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.command;

import com.example.client.admincli.auth.AuthClientFactory;
import com.example.commons.future.FutureUtil;
import com.example.iam.auth.api.dto.LoginResponseDto;
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
@Command(name = "ping", description = "ping to see if there is a connection to the IAM server")
public class PingCommand implements Callable<Integer> {

  private final AuthClientFactory authClientProvider;
  private final PrintStream out;

  @Inject
  PingCommand(@Named("out") PrintStream out, AuthClientFactory authClientProvider) {
    this.authClientProvider = authClientProvider;
    this.out = out;
  }

  @Option(
      defaultValue = "false",
      names = {"-t", "--time"},
      description = "print timing information")
  private boolean timingEnabled;

  @Override
  public Integer call() {
    out.println("called ping");

    if (timingEnabled) {
      out.println("Timing is enabled");
    } else {
      out.println("Timing is disabled");
    }

    IamAuthClient iamAuthClient = authClientProvider.provide();
    Future<LoginResponseDto> login = iamAuthClient.login(null);

    // need to await this future
    FutureUtil.blockingExecution(login);
    LoginResponseDto result = login.result();

    return result != null ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
  }
}
