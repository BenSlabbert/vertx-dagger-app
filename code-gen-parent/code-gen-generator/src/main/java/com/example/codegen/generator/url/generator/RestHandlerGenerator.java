/* Licensed under Apache-2.0 2024. */
package com.example.codegen.generator.url.generator;

import com.example.codegen.generator.commons.GenerationException;
import com.example.codegen.generator.url.annotation.RestHandler;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

    List<PathParser.Param> pathParams = PathParser.parse(path);
    String sanitized = PathSanitizer.sanitize(path);

    Name methodName = ee.getSimpleName();
    Element enclosingElement = ee.getEnclosingElement();
    Name enclosingClassName = enclosingElement.getSimpleName();
    String canonicalName = enclosingElement.asType().toString();
    String classPackage = canonicalName.substring(0, canonicalName.lastIndexOf('.'));

    String string =
        methodName.toString().substring(0, 1).toUpperCase() + methodName.toString().substring(1);

    String generatedClassName = enclosingClassName.toString() + string + "ParamParser";
    String generatedRecordName = enclosingClassName.toString() + string + "Params";

    JavaFileObject builderFile =
        processingEnv.getFiler().createSourceFile(classPackage + "." + generatedClassName);

    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
      out.printf("package %s;%n", classPackage);
      out.println();
      out.println("import com.example.commons.web.IntegerParser;");
      out.println("import com.example.commons.web.LongParser;");
      out.println("import com.example.commons.web.RequestParser;");
      out.println("import com.example.commons.web.StringParser;");
      out.println("import com.example.commons.web.RequestParser;");
      out.println("import io.vertx.ext.web.RoutingContext;");
      out.println();

      out.printf("public final class %s {%n", generatedClassName);
      out.println();
      out.println("\tprivate " + generatedClassName + "() {}");
      out.println();

      out.printf("\tpublic static final String PATH = \"%s\";%n", sanitized);
      out.println();

      out.printf("\tpublic static %s parse(RoutingContext ctx) {%n", generatedRecordName);
      out.println("\t\tRequestParser rp = RequestParser.create(ctx);");
      out.println();

      for (PathParser.Param pathParam : pathParams) {
        String name = pathParam.name();
        String type =
            switch (pathParam.type()) {
              case INT -> "Integer";
              case LONG -> "Long";
              case STRING -> "String";
            };

        out.printf(
            "\t\t%s %s = rp.getPathParam(\"%s\", %sParser.create());%n", type, name, name, type);
      }

      out.println();
      out.printf("\t\treturn new %s(", generatedRecordName);
      String args =
          pathParams.stream().map(PathParser.Param::name).collect(Collectors.joining(", "));
      out.printf("%s", args);
      out.println(");");
      out.println("\t}");
      out.println();

      // print the generated record type
      out.printf("\tpublic record %s(", generatedRecordName);

      String recordArgs =
          pathParams.stream()
              .map(
                  p -> {
                    String name = p.name();
                    String type =
                        switch (p.type()) {
                          case INT -> "int";
                          case LONG -> "long";
                          case STRING -> "String";
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
