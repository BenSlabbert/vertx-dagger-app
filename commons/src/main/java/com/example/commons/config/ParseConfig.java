/* Licensed under Apache-2.0 2023. */
package com.example.commons.config;

import static java.util.logging.Level.INFO;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import lombok.extern.java.Log;

@Log
public class ParseConfig {

  private ParseConfig() {}

  public static Config parseArgs(String[] args) throws IOException {
    if (args.length == 0) return Config.defaults();

    List<String> parsed =
        Arrays.stream(args).filter(s -> !s.startsWith("-X") && !s.startsWith("-D")).toList();

    if (parsed.isEmpty()) return Config.defaults();

    if (parsed.size() != 1) {
      log.info("invalid config, only provide 1 path to a config file");
      System.exit(1);
    }

    log.log(INFO, "parsing config from: {0}", new Object[] {parsed});
    String json = Files.readString(Paths.get(parsed.get(0)));
    return Config.fromJson((JsonObject) Json.decodeValue(json));
  }
}
