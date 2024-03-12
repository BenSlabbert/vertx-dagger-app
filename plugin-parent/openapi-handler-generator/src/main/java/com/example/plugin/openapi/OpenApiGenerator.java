/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

import com.example.plugin.openapi.utils.StringUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.plugin.MojoExecutionException;

@Named
@Singleton
class OpenApiGenerator implements Generator {

  @Override
  public void generate(
      List<RequestResponseSchema> schemas, String basePackage, File outputDirectory)
      throws MojoExecutionException {

    Set<GeneratePojo> generatedPojos = generatePojos(schemas, basePackage, outputDirectory);
  }

  private Set<GeneratePojo> generatePojos(
      List<RequestResponseSchema> schemas, String basePackage, File outputDirectory)
      throws MojoExecutionException {

    String objectSchemaPackage = basePackage + ".model";
    String dir = basePackage.replaceAll("\\.", "/");
    Path outputDirectoryPath = outputDirectory.toPath().resolve(dir).resolve("model");

    Stream<ObjectSchema> s1 =
        schemas.stream()
            .map(RequestResponseSchema::requestBodySchema)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(RequestBodySchema::objectSchema);

    Stream<ObjectSchema> s2 =
        schemas.stream()
            .map(RequestResponseSchema::responseSchema)
            .filter(s -> !s.empty())
            .map(ResponseSchema::objectSchema);

    Set<ObjectSchema> objectSchemas = Stream.concat(s1, s2).collect(Collectors.toSet());
    Set<GeneratePojo> generatedPojos = HashSet.newHashSet(objectSchemas.size());
    for (var os : objectSchemas) {
      GeneratePojo generatedPojo = generatePojo(outputDirectoryPath, objectSchemaPackage, os);
      generatedPojos.add(generatedPojo);
    }
    return generatedPojos;
  }

  private GeneratePojo generatePojo(Path path, String basePackage, ObjectSchema objectSchema)
      throws MojoExecutionException {
    try {
      Files.createDirectories(path);
    } catch (IOException e) {
      throw new MojoExecutionException(e);
    }
    String className = StringUtils.capitalizeFirstChar(objectSchema.name());
    String classPath = className + ".java";
    Path resolve = path.resolve(classPath);

    try (var writer = new PrintWriter(Files.newBufferedWriter(resolve, UTF_8, CREATE_NEW, WRITE))) {
      writeFileContents(basePackage, objectSchema, writer, className);
    } catch (IOException e) {
      throw new MojoExecutionException(e);
    }

    return new GeneratePojo(className, objectSchema);
  }

  record GeneratePojo(String className, ObjectSchema objectSchema) {}

  private void writeFileContents(
      String basePackage, ObjectSchema objectSchema, PrintWriter writer, String className) {
    writer.println("package " + basePackage + ";");
    writer.println();
    writer.println("import com.google.auto.value.AutoBuilder;");
    writer.println();

    writer.printf("public record %s(", className);

    String recordComponents =
        objectSchema.properties().stream()
            .map(p -> p.type().print() + " " + p.name())
            .collect(Collectors.joining(", "));

    writer.printf("%s) {%n", recordComponents);
    writer.println();

    // add google auto builder
    writer.println("\tpublic static Builder builder() {");
    writer.printf("\t\treturn new AutoBuilder_%s_Builder();%n", className);
    writer.println("\t}");
    writer.println();
    writer.println("\t@AutoBuilder");
    writer.println("\tpublic interface Builder {");

    for (var property : objectSchema.properties()) {
      String name = property.name();
      ParamType type = property.type();
      writer.printf("\t\tBuilder %s(%s %s);%n%n", name, type.print(), name);
    }

    writer.printf("\t\t%s build();%n", className);
    writer.println("\t}");
    writer.println("}");
  }
}
