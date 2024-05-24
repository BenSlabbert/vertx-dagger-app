/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

import com.example.plugin.openapi.type.ParamType;
import com.example.plugin.openapi.utils.FileUtils;
import com.example.plugin.openapi.utils.StringUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    Set<GeneratedPojo> generatedPojos = generatePojos(schemas, basePackage, outputDirectory);
    Set<GeneratedParameterParser> generatedParameterParsers =
        generateParameterParsers(schemas, basePackage, outputDirectory);

    // generate an interface to implement
    for (RequestResponseSchema schema : schemas) {}
  }

  private Set<GeneratedParameterParser> generateParameterParsers(
      List<RequestResponseSchema> schemas, String basePackage, File outputDirectory)
      throws MojoExecutionException {

    String objectSchemaPackage = basePackage + ".parameterparser";
    String dir = basePackage.replace('.', '/');
    Path outputDirectoryPath = outputDirectory.toPath().resolve(dir).resolve("parameterparser");

    Set<GeneratedParameterParser> generatedParameterParsers = new HashSet<>();
    for (RequestResponseSchema schema : schemas) {
      List<ParameterSchema> parameters = schema.parameters();
      String className = StringUtils.getParameterParserClassName(schema.method(), schema.path());

      FileUtils.createRequiredDirectories(outputDirectoryPath);

      String classPath = className + ".java";
      Path resolve = outputDirectoryPath.resolve(classPath);
      try (var writer =
          new PrintWriter(Files.newBufferedWriter(resolve, UTF_8, CREATE_NEW, WRITE))) {
        writeParameterParserFileContents(writer, objectSchemaPackage, className, parameters);
        generatedParameterParsers.add(new GeneratedParameterParser(className, schema));
      } catch (IOException e) {
        throw new MojoExecutionException(e);
      }
    }
    return generatedParameterParsers;
  }

  record GeneratedParameterParser(String className, RequestResponseSchema schema) {}

  private void writeParameterParserFileContents(
      PrintWriter writer,
      String objectSchemaPackage,
      String className,
      List<ParameterSchema> parameters) {
    writer.println("package " + objectSchemaPackage + ";");
    writer.println();
    writer.println("import github.benslabbert.vertxdaggercommons.web.IntegerParser;");
    writer.println("import github.benslabbert.vertxdaggercommons.web.LongParser;");
    writer.println("import github.benslabbert.vertxdaggercommons.web.RequestParser;");
    writer.println("import github.benslabbert.vertxdaggercommons.web.StringParser;");
    writer.println("import github.benslabbert.vertxdaggercommons.web.BooleanParser;");
    writer.println("import github.benslabbert.vertxdaggercommons.web.FloatParser;");
    writer.println("import github.benslabbert.vertxdaggercommons.web.DoubleParser;");
    writer.println("import github.benslabbert.vertxdaggercommons.web.InstantParser;");
    writer.println("import github.benslabbert.vertxdaggercommons.web.RequestParser;");
    writer.println("import io.vertx.ext.web.RoutingContext;");
    writer.println("import java.time.Instant;");
    writer.println("import javax.annotation.processing.Generated;");
    writer.println();

    writer.printf(
        "@Generated(value = \"%s\", date = \"%s\")%n",
        getClass().getCanonicalName(), LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
    writer.printf("public final class %s {%n", className);
    writer.println();
    writer.println("\tprivate " + className + "() {}");
    writer.println();

    if (parameters.isEmpty()) {
      writer.println("}");
      return;
    }

    // create a static method to parse the path and query parameters
    writer.printf("\tpublic static ParsedParameters parse(RoutingContext ctx) {%n");
    writer.println("\t\tRequestParser rp = RequestParser.create(ctx);");
    writer.println();
    for (var parameter : parameters) {
      String name = parameter.name();
      ParamType type = parameter.type();
      writer.printf(
          "\t\t%s %s = rp.getQueryParam(\"%s\", %sParser.create());%n",
          type.print(), StringUtils.variableName(name), name, type.printParserPrefix());
    }
    writer.println();
    writer.print("\t\treturn new ParsedParameters(");

    String varNames =
        parameters.stream()
            .map(p -> StringUtils.variableName(p.name()))
            .collect(Collectors.joining(", "));
    writer.print(varNames);

    writer.printf(");%n");

    writer.println("\t}");
    writer.println();
    // create a record to hold the parsed parameters
    writer.print("\tpublic record ParsedParameters(");
    String recordComponents =
        parameters.stream()
            .map(p -> p.type().print() + " " + StringUtils.variableName(p.name()))
            .collect(Collectors.joining(", "));
    writer.printf("%s) {}%n", recordComponents);
    writer.println("}");
  }

  private Set<GeneratedPojo> generatePojos(
      List<RequestResponseSchema> schemas, String basePackage, File outputDirectory)
      throws MojoExecutionException {

    String objectSchemaPackage = basePackage + ".model";
    String dir = basePackage.replace('.', '/');
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
    Set<GeneratedPojo> generatedPojos = HashSet.newHashSet(objectSchemas.size());
    for (var os : objectSchemas) {
      GeneratedPojo generatedPojo = generatePojo(outputDirectoryPath, objectSchemaPackage, os);
      generatedPojos.add(generatedPojo);
    }
    return generatedPojos;
  }

  private GeneratedPojo generatePojo(Path path, String basePackage, ObjectSchema objectSchema)
      throws MojoExecutionException {

    FileUtils.createRequiredDirectories(path);
    String className = StringUtils.capitalizeFirstChar(objectSchema.name());
    String classPath = className + ".java";
    Path resolve = path.resolve(classPath);

    try (var writer = new PrintWriter(Files.newBufferedWriter(resolve, UTF_8, CREATE_NEW, WRITE))) {
      writePojoFileContents(basePackage, objectSchema, writer, className);
    } catch (IOException e) {
      throw new MojoExecutionException(e);
    }

    return new GeneratedPojo(className, objectSchema);
  }

  record GeneratedPojo(String className, ObjectSchema objectSchema) {}

  private void writePojoFileContents(
      String basePackage, ObjectSchema objectSchema, PrintWriter writer, String className) {
    writer.println("package " + basePackage + ";");
    writer.println();
    writer.println("import com.google.auto.value.AutoBuilder;");
    writer.println("import javax.annotation.processing.Generated;");
    writer.println();

    writer.printf(
        "@Generated(value = \"%s\", date = \"%s\")%n",
        getClass().getCanonicalName(), LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
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
