/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.command;

import com.example.client.admincli.auth.AuthClientFactory;
import com.example.client.admincli.util.DisplayErrorUtil;
import com.example.client.admincli.util.KeyFile;
import com.example.client.admincli.util.KeyFileUtil;
import com.example.commons.future.FutureUtil;
import com.example.iam.auth.api.dto.RefreshRequestDto;
import com.example.iam.auth.api.dto.RefreshResponseDto;
import com.example.starter.iam.auth.client.IamAuthClient;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Singleton
@Command(name = "refresh", description = "refresh a user in the IAM server")
public class RefreshCommand implements Callable<Integer> {

  private final IamAuthClient iamAuthClient;
  private final PrintStream out;
  private final Vertx vertx;

  @Inject
  RefreshCommand(@Named("out") PrintStream out, AuthClientFactory authClientProvider, Vertx vertx) {
    this.iamAuthClient = authClientProvider.provide();
    this.out = out;
    this.vertx = vertx;
  }

  @ArgGroup(exclusive = true, multiplicity = "1")
  AuthOptions authOptions;

  @Override
  public Integer call() {
    out.println("execute refresh");

    CredentialProvider credentials = authOptions.toCredentials();
    RefreshRequestDto requestDto = getRequestDto(credentials);

    Future<RefreshResponseDto> resp = FutureUtil.runFutureSync(iamAuthClient.refresh(requestDto));

    if (resp.failed()) {
      return DisplayErrorUtil.handleFailure(out, resp);
    }

    RefreshResponseDto result = resp.result();

    if (CredentialProvider.Kind.KEY_FILE == credentials.getKind()) {
      KeyFileUtil.updateTokens(
          vertx, credentials.keyFile().file(), result.token(), result.refreshToken());
    }

    return result != null ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
  }

  private RefreshRequestDto getRequestDto(CredentialProvider credentials) {
    return switch (credentials.getKind()) {
      case TOKEN -> {
        out.println("using basic auth");
        yield RefreshRequestDto.builder()
            .username(credentials.token().username())
            .token(credentials.token().token())
            .build();
      }
      case KEY_FILE -> {
        out.println("using key file");
        KeyFile keyFile = KeyFileUtil.parse(vertx, credentials.keyFile().file());
        yield RefreshRequestDto.builder()
            .username(keyFile.username())
            .token(keyFile.refreshToken())
            .build();
      }
    };
  }
}
