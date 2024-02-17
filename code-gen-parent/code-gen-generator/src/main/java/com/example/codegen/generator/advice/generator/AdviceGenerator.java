/* Licensed under Apache-2.0 2023. */
package com.example.codegen.generator.advice.generator;

import com.example.codegen.generator.advice.annotation.Advised;
import com.example.codegen.generator.commons.GenerationException;
import com.example.codegen.generator.commons.ReturnTypeWithImports;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.inject.Singleton;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class AdviceGenerator extends AbstractProcessor {

  private static final String OPTION = "option";
  private static final String PROCESS_CUSTOM = "processCustom";

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
    return Set.of(OPTION, PROCESS_CUSTOM);
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
    ExecutableElement constructor = getConstructor(elementToBeAdvised);

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

    if (elementToBeAdvised.getModifiers().contains(Modifier.FINAL)) {
      throw new GenerationException("cannot advise final class");
    }

    boolean isPublic = elementToBeAdvised.getModifiers().contains(Modifier.PUBLIC);
    boolean isSingleton = elementToBeAdvised.getAnnotation(Singleton.class) != null;

    String generatedClassName = superClass + "_Advised";

    Set<String> customAnnotationCanonicalNames =
        methods.stream()
            .flatMap(method -> getAdditionalAnnotations(method).stream())
            .map(CustomAdvisorAnnotation::advisorCanonicalClassName)
            .collect(Collectors.toSet());

    Set<String> canonicalImports =
        getCanonicalImports(superConstructor, methods, advisors).stream()
            .filter(f -> !f.equals("byte"))
            .filter(f -> !f.equals("short"))
            .filter(f -> !f.equals("int"))
            .filter(f -> !f.equals("long"))
            .filter(f -> !f.equals("double"))
            .filter(f -> !f.equals("float"))
            .filter(f -> !f.startsWith("java.lang."))
            .filter(f -> !f.contains("[]"))
            .filter(f -> !f.startsWith(classPackage + "."))
            .collect(Collectors.toSet());

    JavaFileObject builderFile =
        processingEnv.getFiler().createSourceFile(classPackage + "." + generatedClassName);

    // dependencies
    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
      out.printf("package %s;%n", classPackage);
      out.println();

      if (isSingleton) {
        canonicalImports.add("javax.inject.Singleton");
      }

      canonicalImports.add("javax.inject.Inject");
      canonicalImports.add("javax.inject.Provider");
      canonicalImports.addAll(customAnnotationCanonicalNames);

      canonicalImports.stream()
          .sorted(Comparator.naturalOrder())
          .forEach(canonicalImport -> out.printf("import %s;%n", canonicalImport));
      out.println();

      if (isSingleton) {
        out.println("@Singleton");
      }

      if (isPublic) {
        out.print("public ");
      }

      out.printf("class %s extends %s {%n", generatedClassName, superClass);
      out.println();
      out.println("\tprivate static final Class<?> clazz = " + superClass + ".class;");
      out.println();
      printConstructor(
          out, generatedClassName, superConstructor, advisors, customAnnotationCanonicalNames);
      out.println();
      printMethods(out, methods, advisors);
      out.println("}");
    }
  }

  private void printMethods(
      PrintWriter out, List<ExecutableElement> methods, List<Element> advisors) {

    for (ExecutableElement method : methods) {
      List<CustomAdvisorAnnotation> additionalAnnotations = getAdditionalAnnotations(method);

      String modifier =
          method.getModifiers().isEmpty()
              ? ""
              : method.getModifiers().iterator().next().toString() + " ";
      Name methodName = method.getSimpleName();

      ReturnTypeWithImports returnTypeWithImports =
          ReturnTypeWithImports.of(method.getReturnType());
      String returnType = returnTypeWithImports.printableName();

      List<String> methodParams =
          method.getParameters().stream()
              .map(VariableElement::asType)
              // toString does not help for generics
              // need to (TypeVariable) typeMirror to get bounds, etc
              .map(TypeMirror::toString)
              .toList();

      AtomicInteger index = new AtomicInteger();
      List<Pair> pairs =
          methodParams.stream()
              .map(p -> p.substring(p.lastIndexOf(".") + 1))
              .map(s -> new Pair(s, asVariableName(s + "_" + index.getAndIncrement())))
              .toList();

      String collect =
          pairs.stream()
              .map(s -> "%s %s".formatted(s.left, s.right))
              .collect(Collectors.joining(", "));

      out.println("\t@Override");
      out.println("\t" + modifier + returnType + " " + methodName + "(" + collect + ") {");

      String varList = pairs.stream().map(Pair::right).collect(Collectors.joining(", "));

      List<String> additionalAdvisors = new ArrayList<>();
      // call advisors
      for (CustomAdvisorAnnotation additionalAnnotation : additionalAnnotations) {
        String advisor = additionalAnnotation.advisorCanonicalClassName();
        String collected =
            additionalAnnotation.customizers.stream()
                .map(Pair::right)
                .collect(Collectors.joining(", "));

        String adviceGetter = asVariableName(advisor);
        String variableName = "_" + adviceGetter;

        out.println("\t\tvar " + variableName + " = " + adviceGetter + ".get();");

        // customize the advisor
        out.println("\t\t" + variableName + ".customize(" + collected + ");");

        additionalAdvisors.add(variableName);
      }

      advisors.stream()
          .map(Element::asType)
          .map(TypeMirror::toString)
          .map(f -> f.substring(f.lastIndexOf(".") + 1))
          .map(AdviceGenerator::asVariableName)
          .forEach(additionalAdvisors::add);

      for (int i = 0; i < additionalAdvisors.size(); i++) {
        String advisorVariable = additionalAdvisors.get(i);

        if (!advisorVariable.startsWith("_")) {
          String newVar = "_" + advisorVariable;
          out.println("\t\tvar " + newVar + " = " + advisorVariable + ".get();");
          advisorVariable = newVar;
          additionalAdvisors.set(i, advisorVariable);
        }

        if (varList.isEmpty()) {
          out.println("\t\t" + advisorVariable + ".before(" + "clazz, \"" + methodName + "\");");
        } else {
          out.println(
              "\t\t"
                  + advisorVariable
                  + ".before("
                  + "clazz, \""
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

        for (String advisorVariable : additionalAdvisors) {
          out.println(
              "\t\t" + advisorVariable + ".after(" + "clazz, \"" + methodName + "\", _res);");
        }

        out.println();
        out.println("\t\treturn _res;");
      } else {
        // call super and we are done for void methods
        out.println("\t\tsuper." + methodName + "(" + varList + ");");
        out.println();

        for (String advisorVariable : additionalAdvisors) {
          out.println(
              "\t\t" + advisorVariable + ".after(" + "clazz, \"" + methodName + "\", null);");
        }
      }

      out.println("\t}");
      out.println();
    }
  }

  private List<CustomAdvisorAnnotation> getAdditionalAnnotations(ExecutableElement method) {
    List<CustomAdvisorAnnotation> list = new ArrayList<>();

    for (AnnotationMirror annotationMirror : method.getAnnotationMirrors()) {
      DeclaredType declaredType = annotationMirror.getAnnotationType();
      TypeElement declaredTypeElement = (TypeElement) declaredType.asElement();
      ElementKind kind = declaredTypeElement.getKind();

      String args = processingEnv.getOptions().get(PROCESS_CUSTOM);
      Set<String> customAdvisors = Arrays.stream(args.split(",")).collect(Collectors.toSet());

      if (kind != ElementKind.ANNOTATION_TYPE
          || !customAdvisors.contains(declaredTypeElement.toString())) {
        continue;
      }

      list.add(getCustomAdvisor(annotationMirror, declaredTypeElement));
    }

    return list;
  }

  private CustomAdvisorAnnotation getCustomAdvisor(
      AnnotationMirror annotationMirror, TypeElement typeElement) {
    // customizer fields
    List<ExecutableElement> annotationMethods =
        typeElement.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.METHOD)
            .map(e -> (ExecutableElement) e)
            .toList();

    List<VariableElement> annotationFields =
        typeElement.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.FIELD)
            .map(e -> (VariableElement) e)
            .toList();

    if (annotationFields.size() != 1) {
      throw new GenerationException("should only have one variable");
    }

    VariableElement advisorField = annotationFields.getFirst();

    if (!"ADVISOR".equals(advisorField.getSimpleName().toString())) {
      throw new GenerationException("first field must be called advisor");
    }

    // default customizers
    List<Pair> customizers = new ArrayList<>();
    for (ExecutableElement field : annotationMethods) {
      customizers.add(
          new Pair(field.getSimpleName().toString(), field.getDefaultValue().toString()));
    }

    // tells me how the annotation is used
    // update the defaults with actual values
    var elementValues = annotationMirror.getElementValues();
    for (var entry : elementValues.entrySet()) {
      ExecutableElement k = entry.getKey();
      AnnotationValue v = entry.getValue();
      for (int i = 0; i < customizers.size(); i++) {
        Pair pair = customizers.get(i);
        if (pair.left.equals(k.getSimpleName().toString())) {
          customizers.set(i, new Pair(pair.left, v.toString()));
        }
      }
    }

    return new CustomAdvisorAnnotation(advisorField.getConstantValue().toString(), customizers);
  }

  private void printConstructor(
      PrintWriter out,
      String generatedClassName,
      ExecutableElement constructor,
      List<Element> advisors,
      Set<String> customAnnotationCanonicalNames) {

    List<String> additionalAnnotationsParams =
        customAnnotationCanonicalNames.stream().map(AdviceGenerator::getSimpleName).toList();

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

    advisorParams.forEach(
        s -> out.printf("\tprivate final Provider<%s> %s;%n", s, asVariableName(s)));
    additionalAnnotationsParams.forEach(
        s -> out.printf("\tprivate final Provider<%s> %s;%n", s, asVariableName(s)));
    out.println();

    out.println("\t@Inject");
    out.printf("\t%s(", generatedClassName);

    AtomicInteger index = new AtomicInteger();
    String superParamsJoined =
        String.join(
            ", ",
            Stream.of(superParams)
                .flatMap(List::stream)
                .map(f -> "%s %s".formatted(f, asVariableName(f) + "_" + index.getAndIncrement()))
                .toList());

    String advisorParamsJoined =
        Stream.of(advisorParams, additionalAnnotationsParams)
            .flatMap(List::stream)
            .map(
                f ->
                    "Provider<%s> %s"
                        .formatted(f, asVariableName(f) + "_" + index.getAndIncrement()))
            .collect(Collectors.joining(", "));

    String join;

    if (superParamsJoined.isEmpty()) {
      join = advisorParamsJoined;
    } else {
      join = String.join(", ", superParamsJoined, advisorParamsJoined);
    }

    index.set(0);

    out.print(join);

    out.printf(") {%n");

    out.printf(
        "\t\tsuper(%s);%n",
        String.join(
            ", ",
            superParams.stream()
                .map(s -> asVariableName(s) + "_" + index.getAndIncrement())
                .toList()));

    Stream.of(advisorParams, additionalAnnotationsParams)
        .flatMap(List::stream)
        .forEach(
            s ->
                out.printf(
                    "\t\tthis.%s = %s;%n",
                    asVariableName(s), asVariableName(s) + "_" + index.getAndIncrement()));

    out.printf("\t}%n");
  }

  private static String asVariableName(String name) {
    if (name.contains(".")) {
      name = name.substring(name.lastIndexOf(".") + 1);
    }
    if (name.contains("[]")) {
      String first = name.substring(0, name.indexOf('['));
      String second = name.substring(name.indexOf(']') + 1);
      name = first + "s" + second;
    }
    return name.substring(0, 1).toLowerCase() + name.substring(1);
  }

  private static String getSimpleName(String canonicalName) {
    if (canonicalName.contains(".")) {
      canonicalName = canonicalName.substring(canonicalName.lastIndexOf(".") + 1);
    }
    return canonicalName;
  }

  private static Set<String> getCanonicalImports(
      ExecutableElement constructor, List<ExecutableElement> methods, List<Element> advisors) {
    Set<String> constructorParams = getParamsCanonicalClassNames(constructor);

    Set<String> methodParams =
        methods.stream()
            .map(AdviceGenerator::getParamsCanonicalClassNames)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

    Set<String> methodImports =
        methods.stream()
            .map(m -> ReturnTypeWithImports.of(m.getReturnType()))
            .map(ReturnTypeWithImports::canonicalImports)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

    Set<String> advistorTypes =
        advisors.stream()
            .map(Element::asType)
            .map(TypeMirror::toString)
            .collect(Collectors.toSet());

    return Stream.of(constructorParams, methodParams, methodImports, advistorTypes)
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
      var ignore = annotation.advisors(); // NOSONAR this method invocation thrown
    } catch (MirroredTypesException mte) {
      List<? extends TypeMirror> typeMirrors = mte.getTypeMirrors();

      return typeMirrors.stream().map(typeUtils::asElement).toList();
    }

    throw new GenerationException("expecting MirroredTypesException to be thrown");
  }

  record CustomAdvisorAnnotation(String advisorCanonicalClassName, List<Pair> customizers) {}

  record Pair(String left, String right) {}
}
