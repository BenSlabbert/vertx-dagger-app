/* Licensed under Apache-2.0 2023. */
package com.example.migration;

import java.util.Properties;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FluentConfiguration;

public class FlywayProvider {
  private FlywayProvider() {}

  public static Flyway get(
      String host, int port, String username, String password, String database) {
    var flywayConfig = new FluentConfiguration().configuration(readFlywayProps());

    return Flyway.configure()
        .configuration(flywayConfig)
        .dataSource(
            "jdbc:postgresql://%s:%d/%s".formatted(host, port, database), username, password)
        .baselineVersion(MigrationVersion.fromVersion("0"))
        .target(MigrationVersion.LATEST)
        .load();
  }

  private static Properties readFlywayProps() {
    try (var stream = FlywayProvider.class.getResourceAsStream("/flyway/flyway.conf")) {
      var props = new Properties();
      props.load(stream);
      return props;
    } catch (Exception e) {
      throw new MigrationException(e);
    }
  }
}
