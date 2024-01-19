/* Licensed under Apache-2.0 2023. */
package com.example.entitygenerator;

import com.example.migration.FlywayProvider;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Strategy;
import org.jooq.meta.jaxb.Target;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class EntityGenerator {

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      throw new IllegalArgumentException("expecting 2 args: outputFolder and packageName");
    }

    var outputFolder = args[0];
    var packageName = args[1];
    System.out.println("generating JOOQ into dir " + outputFolder + " with package " + packageName);
    generate(outputFolder, packageName);
  }

  private static void generate(String folderPath, String packageName) throws Exception {
    var imageName =
        DockerImageName.parse("postgres:15-alpine")
            .asCompatibleSubstituteFor(PostgreSQLContainer.IMAGE);

    try (var container = new PostgreSQLContainer<>(imageName)) {
      container.start();

      var flyway =
          FlywayProvider.get("127.0.0.1", container.getMappedPort(5432), "test", "test", "test");
      flyway.clean();
      flyway.migrate();

      var database =
          new Database()
              .withName("org.jooq.meta.postgres.PostgresDatabase")
              .withIncludes(".*")
              .withExcludes(String.join("|", flyway.getConfiguration().getTable()))
              .withInputSchema("public")
              .withOutputSchemaToDefault(true)
              .withRecordVersionFields("version");

      var target = new Target().withPackageName(packageName).withDirectory(folderPath);
      var generatorStrategy = new Strategy();
      var generate =
          new Generate().withComments(true).withJavaTimeTypes(true).withFluentSetters(true);

      var generator =
          new Generator()
              .withDatabase(database)
              .withTarget(target)
              .withGenerate(generate)
              .withStrategy(generatorStrategy);

      var jdbc =
          new Jdbc()
              .withDriver("org.postgresql.Driver")
              .withUrl("jdbc:postgresql://127.0.0.1:" + container.getMappedPort(5432) + "/test")
              .withUser("test")
              .withPassword("test");

      var configuration = new Configuration().withJdbc(jdbc).withGenerator(generator);

      GenerationTool.generate(configuration);
    }
  }
}
