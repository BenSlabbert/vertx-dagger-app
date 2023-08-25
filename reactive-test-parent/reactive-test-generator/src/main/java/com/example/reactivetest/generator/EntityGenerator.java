package com.example.reactivetest.generator;

import com.example.reactivetest.migration.FlywayProvider;
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
    var outputFolder = args.length > 0 ? args[0] : "target/generated-sources/java";
    System.out.println("generating JOOQ into dir " + outputFolder);
    generate(outputFolder);
  }

  public static void generate(String folderPath) throws Exception {
    var imageName =
        DockerImageName.parse("postgres:15-alpine")
            .asCompatibleSubstituteFor(PostgreSQLContainer.IMAGE);

    try (var container = new PostgreSQLContainer<>(imageName)) {
      container.start();

      var flyway =
          FlywayProvider.get("localhost", container.getMappedPort(5432), "test", "test", "test");
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

      var target =
          new Target()
              .withPackageName("com.example.reactivetest.generator.entity.generated.jooq")
              .withDirectory(folderPath);
      var generatorStrategy = new Strategy();
      var generate = new Generate().withJavaTimeTypes(true);

      var generator =
          new Generator()
              .withDatabase(database)
              .withTarget(target)
              .withGenerate(generate)
              .withStrategy(generatorStrategy);

      var jdbc =
          new Jdbc()
              .withDriver("org.postgresql.Driver")
              .withUrl("jdbc:postgresql://localhost:" + container.getMappedPort(5432) + "/test")
              .withUser("test")
              .withPassword("test");

      var configuration = new Configuration().withJdbc(jdbc).withGenerator(generator);

      GenerationTool.generate(configuration);
    }
  }
}
