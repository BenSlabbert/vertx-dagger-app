/* Licensed under Apache-2.0 2023. */
package com.example.codegen.generator.generator;

import com.example.codegen.generator.annotation.Advised;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class AdviceGenerator extends AbstractProcessor {

  private static final String OPTION = "option";

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(Advised.class.getCanonicalName());
  }

  @Override
  public Set<String> getSupportedOptions() {
    return Set.of(OPTION);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (annotations.isEmpty()) {
      return false;
    }

    String option = processingEnv.getOptions().get(OPTION);
    System.err.println("option: " + option);

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
    System.err.println("class to be advised: " + elementToBeAdvised.getSimpleName());

    ExecutableElement constructor = getConstructor(elementToBeAdvised);
    System.err.println("constructor: " + constructor);
    // need to take in javax.inject.Provider as a parameter
    // so for each invocation we can create a new instance of the advice

    List<ExecutableElement> methods = getMethods(elementToBeAdvised);
    List<Element> advisors = getAdvisors(elementToBeAdvised);

    generateFile(elementToBeAdvised, constructor, methods, advisors);
  }

  private void generateFile(
      Element elementToBeAdvised,
      ExecutableElement superConstructor,
      List<ExecutableElement> methods,
      List<Element> advisors)
      throws IOException {

    String canonicalName = elementToBeAdvised.asType().toString();
    String classPackage = canonicalName.substring(0, canonicalName.lastIndexOf('.'));
    Name superClass = elementToBeAdvised.getSimpleName();
    String generatedClassName = superClass + "_Advised";

    Set<String> canonicalImports =
        getCanonicalImports(superConstructor, methods, advisors).stream()
            .filter(f -> !f.equals("byte"))
            .filter(f -> !f.equals("short"))
            .filter(f -> !f.equals("int"))
            .filter(f -> !f.equals("long"))
            .filter(f -> !f.equals("double"))
            .filter(f -> !f.equals("float"))
            .filter(f -> !f.startsWith("java.lang."))
            .filter(f -> !f.startsWith(classPackage + "."))
            .collect(Collectors.toSet());

    JavaFileObject builderFile =
        processingEnv.getFiler().createSourceFile(classPackage + "." + generatedClassName);

    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
      out.printf("package %s;%n", classPackage);
      out.println();

      canonicalImports.forEach(canonicalImport -> out.printf("import %s;%n", canonicalImport));
      if (!canonicalImports.isEmpty()) {
        out.println();
      }

      out.println("@javax.inject.Singleton");
      out.printf("public class %s extends %s {%n", generatedClassName, superClass);
      out.println();
      printConstructor(out, generatedClassName, superConstructor, advisors);
      out.println();
      printMethods(out, methods, advisors, superClass);
      out.println("}");
    }
  }

  private void printMethods(
      PrintWriter out, List<ExecutableElement> methods, List<Element> advisors, Name superClass) {

    for (ExecutableElement method : methods) {
      String modifier =
          method.getModifiers().isEmpty()
              ? ""
              : method.getModifiers().iterator().next().toString() + " ";
      Name methodName = method.getSimpleName();
      String returnType = method.getReturnType().toString();

      List<String> methodParams =
          method.getParameters().stream()
              .map(VariableElement::asType)
              .map(TypeMirror::toString)
              .toList();

      AtomicInteger index = new AtomicInteger();
      List<Pair> pairs =
          methodParams.stream()
              .map(p -> p.substring(p.lastIndexOf(".") + 1))
              .map(s -> new Pair(s, asVariableName(s + "_" + index.getAndIncrement())))
              .toList();

      String collect =
          pairs.stream().map(s -> "%s %s".formatted(s.l, s.r)).collect(Collectors.joining(", "));

      out.println("\t@Override");
      out.println("\t" + modifier + returnType + " " + methodName + "(" + collect + ") {");
      out.println();

      String varList = pairs.stream().map(Pair::r).collect(Collectors.joining(", "));

      // call advisors
      List<String> advisorVariables =
          advisors.stream()
              .map(Element::asType)
              .map(TypeMirror::toString)
              .map(f -> f.substring(f.lastIndexOf(".") + 1))
              .map(AdviceGenerator::asVariableName)
              .toList();

      for (String advisorVariable : advisorVariables) {
        if (varList.isEmpty()) {
          out.println(
              "\t\t"
                  + advisorVariable
                  + ".before("
                  + superClass
                  + ".class, \""
                  + methodName
                  + "\");");
        } else {
          out.println(
              "\t\t"
                  + advisorVariable
                  + ".before("
                  + superClass
                  + ".class, \""
                  + methodName
                  + "\", "
                  + varList
                  + ");");
        }
      }

      out.println();

      // call super

      if (!"void".equals(returnType)) {
        out.println("\t\tvar _res = super." + methodName + "(" + varList + ");");
        out.println();

        for (String advisorVariable : advisorVariables) {
          out.println(
              "\t\t"
                  + advisorVariable
                  + ".after("
                  + superClass
                  + ".class, \""
                  + methodName
                  + "\", _res);");
        }

        out.println();
        out.println("\t\treturn _res;");
      } else {
        // call super and we are done for void methods
        out.println("\t\tsuper." + methodName + "(" + varList + ");");
        out.println();

        for (String advisorVariable : advisorVariables) {
          out.println(
              "\t\t"
                  + advisorVariable
                  + ".after("
                  + superClass
                  + ".class, \""
                  + methodName
                  + "\", null);");
        }
      }

      out.println("\t}");
      out.println();
    }
  }

  private void printConstructor(
      PrintWriter out,
      String generatedClassName,
      ExecutableElement constructor,
      List<Element> advisors) {

    List<String> superParams =
        getParamsCanonicalClassNames(constructor).stream()
            .map(f -> f.substring(f.lastIndexOf(".") + 1))
            .toList();

    List<String> advisorParams =
        advisors.stream()
            .map(Element::asType)
            .map(TypeMirror::toString)
            .map(f -> f.substring(f.lastIndexOf(".") + 1))
            .toList();

    advisorParams.forEach(s -> out.printf("\tprivate final %s %s;%n", s, asVariableName(s)));
    out.println();

    out.println("\t@javax.inject.Inject");
    out.printf("\t%s(", generatedClassName);

    AtomicInteger index = new AtomicInteger();
    String params =
        String.join(
            ", ",
            Stream.of(superParams, advisorParams)
                .flatMap(List::stream)
                .map(f -> "%s %s".formatted(f, asVariableName(f) + "_" + index.getAndIncrement()))
                .toList());

    index.set(0);

    out.print(params);

    out.printf(") {%n");

    out.printf(
        "\t\tsuper(%s);%n",
        String.join(
            ", ",
            superParams.stream()
                .map(s -> asVariableName(s) + "_" + index.getAndIncrement())
                .toList()));

    advisorParams.forEach(
        s ->
            out.printf(
                "\t\tthis.%s = %s;%n",
                asVariableName(s), asVariableName(s) + "_" + index.getAndIncrement()));

    out.printf("\t}%n");
  }

  private static String asVariableName(String name) {
    return name.substring(0, 1).toLowerCase() + name.substring(1);
  }

  private static Set<String> getCanonicalImports(
      ExecutableElement constructor, List<ExecutableElement> methods, List<Element> advisors) {
    Set<String> constructorParams = getParamsCanonicalClassNames(constructor);

    Set<String> methodParams =
        methods.stream()
            .map(AdviceGenerator::getParamsCanonicalClassNames)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

    Set<String> advistorTypes =
        advisors.stream()
            .map(Element::asType)
            .map(TypeMirror::toString)
            .collect(Collectors.toSet());

    return Stream.of(constructorParams, methodParams, advistorTypes)
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  private static Set<String> getParamsCanonicalClassNames(ExecutableElement e) {
    return e.getParameters().stream()
        .map(VariableElement::asType)
        .map(TypeMirror::toString)
        .collect(Collectors.toSet());
  }

  private static List<ExecutableElement> getMethods(Element element) {
    return ElementFilter.methodsIn(element.getEnclosedElements()).stream()
        .filter(AdviceGenerator::filterModifiers)
        .toList();
  }

  private static ExecutableElement getConstructor(Element element) {
    return ElementFilter.constructorsIn(element.getEnclosedElements()).stream()
        .filter(AdviceGenerator::filterModifiers)
        .findFirst()
        .orElseThrow();
  }

  private static boolean filterModifiers(ExecutableElement f) {
    return f.getModifiers().contains(Modifier.PUBLIC)
        || f.getModifiers().contains(Modifier.PROTECTED)
        || f.getModifiers().isEmpty();
  }

  private List<Element> getAdvisors(Element element) {
    Types typeUtils = processingEnv.getTypeUtils();

    try {
      Advised annotation = element.getAnnotation(Advised.class);
      annotation.advisors();
    } catch (MirroredTypesException mte) {
      List<? extends TypeMirror> typeMirrors = mte.getTypeMirrors();

      return typeMirrors.stream().map(typeUtils::asElement).toList();
    }

    throw new GenerationException("expecting MirroredTypesException to be thrown");
  }

  record Pair(String l, String r) {}
}
