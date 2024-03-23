/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli;

import com.example.client.admincli.command.LoginCommand;
import com.example.client.admincli.command.RefreshCommand;
import com.example.client.admincli.command.RegisterCommand;
import com.example.client.admincli.config.IamConfig;
import com.example.client.admincli.ioc.DaggerProvider;
import com.example.client.admincli.ioc.Provider;
import com.example.commons.future.FutureUtil;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;

@Command(
    name = "greet",
    mixinStandardHelpOptions = true,
    version = "checksum 4.0",
    description = "default command",
    subcommands = {RegisterCommand.class, LoginCommand.class, RefreshCommand.class})
public class Main {

  @Option(
      required = true,
      names = {"-c", "--config"},
      description = "path to config.json")
  static File configFile;

  static class DaggerIFactory implements CommandLine.IFactory {

    private final CommandLine.IFactory defaultFactory = CommandLine.defaultFactory();
    private Provider provider;

    @Override
    public synchronized <K> K create(Class<K> aClass) throws Exception {
      if (provider == null) {
        VertxOptions vertxOptions = new VertxOptions().setPreferNativeTransport(true);
        Vertx vertx = Vertx.builder().with(vertxOptions).build();
        vertx.exceptionHandler(err -> System.err.println("unhandled exception: " + err));

        if (null == configFile) {
          throw new IllegalArgumentException("config file is required");
        }

        Buffer buffer = vertx.fileSystem().readFileBlocking(configFile.getAbsolutePath());
        JsonObject cfg = new JsonObject(buffer.toString(StandardCharsets.UTF_8));
        provider = DaggerProvider.builder().vertx(vertx).iamConfig(IamConfig.fromJson(cfg)).build();
      }

      Map<Class<?>, Object> map = provider.commandByClass();

      if (map.containsKey(aClass)) {
        return (K) map.get(aClass);
      }

      return defaultFactory.create(aClass);
    }
  }

  private Main() {}

  public static void main(String... args) {
    int exitCode =
        new CommandLine(new Main(), new DaggerIFactory())
            .setColorScheme(CommandLine.Help.defaultColorScheme(Ansi.AUTO))
            .execute(args);

    FutureUtil.awaitTerminationSync();

    System.exit(exitCode);
  }
}
