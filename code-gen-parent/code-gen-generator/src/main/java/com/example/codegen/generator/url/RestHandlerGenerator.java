/* Licensed under Apache-2.0 2024. */
package com.example.codegen.generator.url;

import com.example.codegen.annotation.url.RestHandler;
import com.example.codegen.generator.commons.GenerationException;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class RestHandlerGenerator extends AbstractProcessor {

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(RestHandler.class.getCanonicalName());
  }

  @Override
  public Set<String> getSupportedOptions() {
    return Set.of();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (annotations.isEmpty()) {
      return false;
    }

    for (TypeElement annotation : annotations) {
      Set<? extends Element> annotated = roundEnv.getElementsAnnotatedWith(annotation);
      for (Element element : annotated) {
        try {
          process(element);
        } catch (Exception e) {
          throw new GenerationException(e);
        }
      }
    }

    return true;
  }

  private void process(Element elementToBeAdvised) throws IOException {
    if (elementToBeAdvised.asType().getKind() != TypeKind.EXECUTABLE) {
      throw new GenerationException(
          "RestHandler annotation can only be used on methods, but was used on: "
              + elementToBeAdvised);
    }

    ExecutableElement ee = (ExecutableElement) elementToBeAdvised;
    String path = getPath(ee);

    PathParser.ParseResult parseResult = PathParser.parse(path);
    String sanitized = PathSanitizer.sanitize(path);

    Name methodName = ee.getSimpleName();
    Element enclosingElement = ee.getEnclosingElement();
    Name enclosingClassName = enclosingElement.getSimpleName();
    String canonicalName = enclosingElement.asType().toString();
    String classPackage = canonicalName.substring(0, canonicalName.lastIndexOf('.'));

    String string =
        methodName.toString().substring(0, 1).toUpperCase() + methodName.toString().substring(1);

    String generatedClassName = enclosingClassName.toString() + "_" + string + "_" + "ParamParser";
    String generatedRecordName = enclosingClassName.toString() + "_" + string + "_" + "Params";

    JavaFileObject builderFile =
        processingEnv.getFiler().createSourceFile(classPackage + "." + generatedClassName);

    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
      out.printf("package %s;%n", classPackage);
      out.println();
      out.println("import com.example.commons.web.IntegerParser;");
      out.println("import com.example.commons.web.LongParser;");
      out.println("import com.example.commons.web.RequestParser;");
      out.println("import com.example.commons.web.StringParser;");
      out.println("import com.example.commons.web.BooleanParser;");
      out.println("import com.example.commons.web.FloatParser;");
      out.println("import com.example.commons.web.DoubleParser;");
      out.println("import com.example.commons.web.InstantParser;");
      out.println("import com.example.commons.web.RequestParser;");
      out.println("import io.vertx.ext.web.RoutingContext;");
      out.println("import javax.annotation.processing.Generated;");
      out.println("import java.time.Instant;");
      out.println();

      out.printf(
          "@Generated(value = \"%s\", date = \"%s\")%n",
          getClass().getCanonicalName(),
          LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
      out.printf("final class %s {%n", generatedClassName);
      out.println();
      out.println("\tprivate " + generatedClassName + "() {}");
      out.println();

      out.printf("\tstatic final String PATH = \"%s\";%n", sanitized);
      out.println();

      out.printf("\tstatic %s parse(RoutingContext ctx) {%n", generatedRecordName);
      out.println("\t\tRequestParser rp = RequestParser.create(ctx);");
      out.println();

      for (PathParser.Param pathParam : parseResult.pathParams()) {
        String name = pathParam.name();
        String type =
            switch (pathParam.type()) {
              case INT -> "Integer";
              case LONG -> "Long";
              case BOOLEAN -> "Boolean";
              case FLOAT -> "Float";
              case DOUBLE -> "Double";
              case TIMESTAMP -> "Instant";
              case STRING -> "String";
            };

        Optional<String> optional = pathParam.defaultValue();
        if (optional.isPresent()) {
          if (pathParam.type() == PathParser.Type.STRING) {
            out.printf(
                "\t\t%s %s = rp.getPathParam(\"%s\", \"%s\", %sParser.create());%n",
                type, name, name, optional.get(), type);
          } else {
            out.printf(
                "\t\t%s %s = rp.getPathParam(\"%s\", %s, %sParser.create());%n",
                type, name, name, optional.get(), type);
          }
        } else {
          out.printf(
              "\t\t%s %s = rp.getPathParam(\"%s\", %sParser.create());%n", type, name, name, type);
        }
      }

      for (PathParser.Param queryParam : parseResult.queryParams()) {
        String name = queryParam.name();
        String type =
            switch (queryParam.type()) {
              case INT -> "Integer";
              case LONG -> "Long";
              case BOOLEAN -> "Boolean";
              case FLOAT -> "Float";
              case DOUBLE -> "Double";
              case TIMESTAMP -> "Instant";
              case STRING -> "String";
            };

        Optional<String> optional = queryParam.defaultValue();
        if (optional.isPresent()) {
          if (queryParam.type() == PathParser.Type.STRING) {
            out.printf(
                "\t\t%s %s = rp.getQueryParam(\"%s\", \"%s\", %sParser.create());%n",
                type, name, name, optional.get(), type);
          } else {
            out.printf(
                "\t\t%s %s = rp.getQueryParam(\"%s\", %s, %sParser.create());%n",
                type, name, name, optional.get(), type);
          }
        } else {
          out.printf(
              "\t\t%s %s = rp.getQueryParam(\"%s\", %sParser.create());%n", type, name, name, type);
        }
      }

      out.println();
      out.printf("\t\treturn new %s(", generatedRecordName);
      String args =
          Stream.concat(parseResult.pathParams().stream(), parseResult.queryParams().stream())
              .map(PathParser.Param::name)
              .collect(Collectors.joining(", "));
      out.printf("%s", args);
      out.println(");");
      out.println("\t}");
      out.println();

      // print the generated record type
      out.printf("\trecord %s(", generatedRecordName);

      String recordArgs =
          Stream.concat(parseResult.pathParams().stream(), parseResult.queryParams().stream())
              .map(
                  p -> {
                    String name = p.name();
                    String type =
                        switch (p.type()) {
                          case INT -> "int";
                          case LONG -> "long";
                          case STRING -> "String";
                          case BOOLEAN -> "boolean";
                          case FLOAT -> "float";
                          case DOUBLE -> "double";
                          case TIMESTAMP -> "Instant";
                        };

                    return type + " " + name;
                  })
              .collect(Collectors.joining(", "));

      out.printf("%s", recordArgs);
      out.println(") {}");

      out.println("}");
    }
  }

  private String getPath(Element element) {
    RestHandler annotation = element.getAnnotation(RestHandler.class);
    return annotation.path();
  }
}
