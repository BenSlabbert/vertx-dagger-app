package com.example.catalog.migration;

import com.example.commons.config.Config;
import com.example.commons.config.ParseConfig;
import java.io.IOException;
import lombok.extern.java.Log;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

@Log
public class Main {

  public static void main(String[] args) throws IOException {
    Config.PostgresConfig config = ParseConfig.parseArgs(args).postgresConfig();

    Flyway flyway =
        Flyway.configure()
            .locations("classpath:migration")
            .dataSource(
                String.format(
                    "jdbc:postgresql://%s:%d/%s", config.host(), config.port(), config.database()),
                config.username(),
                config.password())
            .load();

    MigrateResult result = flyway.migrate();
    if (!result.success) {
      log.severe("failed to migrate db");
      System.exit(1);
    }

    log.info("migration complete");
  }
}
