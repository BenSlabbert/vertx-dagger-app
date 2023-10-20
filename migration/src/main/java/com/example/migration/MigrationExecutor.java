/* Licensed under Apache-2.0 2023. */
package com.example.migration;

import java.util.Arrays;

public class MigrationExecutor {

  public static void main(String[] args) {
    args =
        Arrays.stream(args)
            .filter(a -> !a.equals("java"))
            .filter(a -> !a.startsWith("-"))
            .filter(a -> !a.endsWith(".jar"))
            .toArray(String[]::new);

    if (args.length != 5)
      throw new IllegalArgumentException("expecting 5 arguments but got: " + Arrays.toString(args));

    var flyway = FlywayProvider.get(args[0], Integer.parseInt(args[1]), args[2], args[3], args[4]);
    flyway.migrate();
  }
}
