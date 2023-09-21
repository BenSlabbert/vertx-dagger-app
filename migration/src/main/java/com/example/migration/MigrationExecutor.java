/* Licensed under Apache-2.0 2023. */
package com.example.migration;

public class MigrationExecutor {

  public static void main(String[] args) {
    if (args.length != 5) throw new IllegalArgumentException("expecting 5 arguments");

    var flyway = FlywayProvider.get(args[0], Integer.parseInt(args[1]), args[2], args[3], args[4]);
    flyway.migrate();
  }
}
