/* Licensed under Apache-2.0 2024. */
package com.example.codegen.generator.security;

import com.example.codegen.annotation.security.SecuredProxy;
import com.example.codegen.generator.commons.GenerationException;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class SecuredProxyGenerator extends AbstractProcessor {

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(SecuredProxy.class.getCanonicalName());
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
    if (ElementKind.INTERFACE != elementToBeAdvised.getKind()) {
      throw new GenerationException(
          "Only interfaces can be annotated with: " + getSupportedAnnotationTypes());
    }

    List<SecuredMethod> securedMethods = getSecuredMethods(elementToBeAdvised);

    generateFile(elementToBeAdvised, securedMethods);
  }

  private void generateFile(Element elementToBeAdvised, List<SecuredMethod> securedMethods)
      throws IOException {

    String canonicalName = elementToBeAdvised.asType().toString();
    String classPackage = canonicalName.substring(0, canonicalName.lastIndexOf('.'));
    Name superClass = elementToBeAdvised.getSimpleName();

    String generatedClassName = superClass + "_SecuredActions";

    JavaFileObject builderFile =
        processingEnv.getFiler().createSourceFile(classPackage + "." + generatedClassName);

    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
      out.printf("package %s;%n", classPackage);
      out.println();
      out.println("import com.example.commons.security.rpc.SecuredAction;");
      out.println();
      out.println("import java.util.List;");
      out.println("import java.util.Map;");
      out.println();

      out.printf("public final class %s {%n", generatedClassName);
      out.println();

      out.println("\tpublic static Map<String, SecuredAction> getSecuredActions() {");
      out.println("\t\treturn Map.ofEntries(");
      boolean isLast = false;
      for (int i = 0; i < securedMethods.size(); i++) {
        if (i == securedMethods.size() - 1) {
          isLast = true;
        }
        String commaStr = isLast ? "" : ",";

        SecuredMethod securedMethod = securedMethods.get(i);
        out.printf(
            "\t\t\tMap.entry(\"%s\", new SecuredAction(\"%s\", \"%s\", List.of(%s)))%s%n",
            securedMethod.methodName(),
            securedMethod.group(),
            securedMethod.role(),
            String.join(
                ", ", securedMethod.permissions().stream().map(s -> "\"" + s + "\"").toList()),
            commaStr);
      }
      out.println("\t\t);");
      out.println("\t}");
      out.println("}");
    }
  }

  private static List<SecuredMethod> getSecuredMethods(Element elementToBeAdvised) {
    List<SecuredMethod> securedMethods = new ArrayList<>();

    elementToBeAdvised.getEnclosedElements().stream()
        .filter(e -> ElementKind.METHOD == e.getKind())
        .forEach(
            e -> {
              var annotation = e.getAnnotation(SecuredProxy.SecuredAction.class);
              if (null != annotation) {
                securedMethods.add(
                    new SecuredMethod(
                        e.getSimpleName().toString(),
                        annotation.group(),
                        annotation.role(),
                        List.of(annotation.permissions())));
              }
            });

    return securedMethods;
  }

  private record SecuredMethod(
      String methodName, String group, String role, List<String> permissions) {}
}
