/* Licensed under Apache-2.0 2023. */
package com.example.codegen.generator.generator;

import com.example.codegen.generator.annotation.BeforeAdvice;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class AdvisedByGenerator extends AbstractProcessor {

  private static final String DUMMY = "dummy";

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(BeforeAdvice.class.getCanonicalName());
  }

  @Override
  public Set<String> getSupportedOptions() {
    return Set.of(DUMMY);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (annotations.isEmpty()) {
      return false;
    }

    // the goal of this is to
    // 1. create an interface with the methods that will be called before, after, around
    // 2. create a subclass of the @Advised class
    // 3. add the generated interface as a final param to the new subclass

    String dummy = processingEnv.getOptions().get(DUMMY);
    System.err.println("dummy option: " + dummy);

    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "starting processor");

    for (TypeElement annotation : annotations) {
      Set<? extends Element> annotated = roundEnv.getElementsAnnotatedWith(annotation);
      for (Element element : annotated) {
        try {
          process(element);
        } catch (IOException e) {
          throw new GenerationException(e);
        }
      }
    }

    return true;
  }

  private TypeElement findEnclosingTypeElement(Element e) {
    while (e != null && !(e instanceof TypeElement)) {
      e = e.getEnclosingElement();
    }

    return TypeElement.class.cast(e);
  }

  private void process(Element element) throws IOException {
    String canonicalName = element.asType().toString();
    String classPackage = canonicalName.substring(0, canonicalName.lastIndexOf('.'));
    Name superClass = element.getSimpleName();

    TypeElement enclosingTypeElement = findEnclosingTypeElement(element);
    Set<Modifier> modifiers = enclosingTypeElement.getModifiers();

    boolean acceptableModifiers =
        modifiers.stream()
            .anyMatch(
                m ->
                    m == Modifier.ABSTRACT
                        || m == Modifier.PUBLIC
                        || m == Modifier.PROTECTED
                        || m == Modifier.DEFAULT);

    if (!acceptableModifiers) {
      throw new GenerationException("Only abstract, public, protected, and default are supported");
    }

    boolean hasFinal = modifiers.stream().anyMatch(m -> m == Modifier.FINAL);

    if (hasFinal) {
      throw new GenerationException("cannot advise a final class");
    }

    // only supports abstract, class, protected

    System.err.println("modifiers: " + modifiers);

    List<ExecutableElement> executableElements =
        ElementFilter.methodsIn(enclosingTypeElement.getEnclosedElements());

    List<ExecutableElement> constructors =
        ElementFilter.constructorsIn(enclosingTypeElement.getEnclosedElements()).stream()
            .filter(
                f ->
                    f.getModifiers().contains(Modifier.PUBLIC)
                        || f.getModifiers().contains(Modifier.PROTECTED))
            .toList();
    constructors.forEach(e -> System.err.println("constructors: " + e));

    List<ExecutableElement> overrideMethods =
        executableElements.stream()
            .filter(
                f ->
                    f.getModifiers().contains(Modifier.PUBLIC)
                        || f.getModifiers().contains(Modifier.PROTECTED))
            .toList();
    overrideMethods.forEach(e -> System.err.println("overrideMethods: " + e));

    //
    // generate interface start
    //
    String generatedInterfaceName = "AdvisorBeforeContract_" + superClass;
    JavaFileObject builderFile =
        processingEnv.getFiler().createSourceFile(classPackage + "." + generatedInterfaceName);

    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
      out.println("package %s;".formatted(classPackage));
      out.println();
      out.println("import java.util.Optional;");
      out.println();
      out.println("public interface %s {".formatted(generatedInterfaceName));
      out.println();

      for (ExecutableElement overrideMethod : overrideMethods) {
        // for the before contract, it is always void
        TypeMirror returnType = overrideMethod.getReturnType();
        List<? extends VariableElement> parameters = overrideMethod.getParameters();

        if (parameters.isEmpty()) {
          if (returnType.getKind() == TypeKind.VOID) {
            out.println(
                "public boolean %s(%s advised);"
                    .formatted(overrideMethod.getSimpleName(), superClass));
          } else {
            out.println(
                "public Optional<%s> %s(%s advised);"
                    .formatted(returnType, overrideMethod.getSimpleName(), superClass));
          }
        } else {
          List<Map.Entry<Name, TypeMirror>> entries =
              parameters.stream().map(p -> Map.entry(p.getSimpleName(), p.asType())).toList();

          if (returnType.getKind() == TypeKind.VOID) {
            out.print("public boolean %s(".formatted(overrideMethod.getSimpleName()));

            for (Map.Entry<Name, TypeMirror> entry : entries) {
              out.print("%s %s, ".formatted(entry.getValue(), entry.getKey()));
            }

            out.println("%s advised);".formatted(superClass));
          } else {
            out.print(
                "public Optional<%s> %s(".formatted(returnType, overrideMethod.getSimpleName()));

            for (Map.Entry<Name, TypeMirror> entry : entries) {
              out.print("%s %s, ".formatted(entry.getValue(), entry.getKey()));
            }

            out.println("%s advised);".formatted(superClass));
          }
        }
      }

      out.println("}");
      out.println();
    }
    //
    // generate interface end
    //

    String generatedClassName = "BeforeAdvisor_" + superClass;

    builderFile =
        processingEnv.getFiler().createSourceFile(classPackage + "." + generatedClassName);

    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
      out.println("package %s;".formatted(classPackage));
      out.println();
      out.println("public class %s extends %s {".formatted(generatedClassName, superClass));
      out.println();

      //
      // fields start
      //
      out.println("private final %s advisor;".formatted(generatedInterfaceName));
      out.println();
      //
      // fields end
      //

      //
      // constructor start
      //
      if (constructors.isEmpty()) {
        out.println(
            "public %s(%s advisor) {".formatted(generatedClassName, generatedInterfaceName));
        out.println("super();");
        out.println("this.advisor = advisor;");
        out.println("}");
        out.println();
      }

      for (ExecutableElement constructor : constructors) {
        List<? extends VariableElement> parameters = constructor.getParameters();

        if (parameters.isEmpty()) {
          out.println(
              "public %s(%s advisor) {".formatted(generatedClassName, generatedInterfaceName));
          out.println("super();");
          out.println("this.advisor = advisor;");
          out.println("}");
          out.println();
        } else {
          List<Map.Entry<Name, TypeMirror>> entries =
              parameters.stream().map(p -> Map.entry(p.getSimpleName(), p.asType())).toList();

          out.print("public %s(%s advisor".formatted(generatedClassName, generatedInterfaceName));
          for (Map.Entry<Name, TypeMirror> entry : entries) {
            out.print(", %s %s".formatted(entry.getValue(), entry.getKey()));
          }
          out.println(") {");

          out.print("super(");
          for (int i = 0; i < entries.size(); i++) {
            Map.Entry<Name, TypeMirror> entry = entries.get(i);
            if (i == entries.size() - 1) {
              out.print("%s".formatted(entry.getKey()));
            } else {
              out.print("%s, ".formatted(entry.getKey()));
            }
          }
          out.println(");");

          out.println("this.advisor = advisor;");
          out.println("}");
          out.println();
        }
      }
      //
      // constructor end
      //

      //
      // calling override advisor methods start
      //
      for (ExecutableElement overrideMethod : overrideMethods) {
        TypeMirror returnType = overrideMethod.getReturnType();
        List<? extends VariableElement> parameters = overrideMethod.getParameters();

        out.println("@Override");

        if (parameters.isEmpty()) {
          out.println("public %s %s() {".formatted(returnType, overrideMethod.getSimpleName()));
        } else {
          List<Map.Entry<Name, TypeMirror>> entries =
              parameters.stream().map(p -> Map.entry(p.getSimpleName(), p.asType())).toList();

          out.print("public %s %s(".formatted(returnType, overrideMethod.getSimpleName()));
          for (int i = 0; i < entries.size(); i++) {
            Map.Entry<Name, TypeMirror> entry = entries.get(i);
            if (i == entries.size() - 1) {
              out.print("%s %s".formatted(entry.getValue(), entry.getKey()));
            } else {
              out.print("%s %s, ".formatted(entry.getValue(), entry.getKey()));
            }
          }
          out.println(") {");
        }

        // call super method
        if (returnType.getKind() == TypeKind.VOID) {
          if (parameters.isEmpty()) {
            out.println(
                "boolean shouldContinue = advisor.%s(this);"
                    .formatted(overrideMethod.getSimpleName()));
            out.println();
            out.println("if (!shouldContinue) {");
            out.println("return;");
            out.println("}");
            out.println();
            out.println("super.%s();".formatted(overrideMethod.getSimpleName()));
          } else {
            List<Map.Entry<Name, TypeMirror>> entries =
                parameters.stream().map(p -> Map.entry(p.getSimpleName(), p.asType())).toList();

            out.print(
                "boolean shouldContinue = advisor.%s(".formatted(overrideMethod.getSimpleName()));

            for (int i = 0; i < entries.size(); i++) {
              Map.Entry<Name, TypeMirror> entry = entries.get(i);
              if (i == entries.size() - 1) {
                out.print("%s".formatted(entry.getKey()));
              } else {
                out.print("%s, ".formatted(entry.getKey()));
              }
            }

            out.println(", this);");
            out.println();

            out.println("if (!shouldContinue) {");
            out.println("return;");
            out.println("}");
            out.println();
            out.print("super.%s(".formatted(overrideMethod.getSimpleName()));

            for (int i = 0; i < entries.size(); i++) {
              Map.Entry<Name, TypeMirror> entry = entries.get(i);
              if (i == entries.size() - 1) {
                out.print("%s".formatted(entry.getKey()));
              } else {
                out.print("%s, ".formatted(entry.getKey()));
              }
            }

            out.println(");");
          }

          out.println("return;");
        } else {
          if (parameters.isEmpty()) {
            out.println("var option = advisor.%s(this);".formatted(overrideMethod.getSimpleName()));
            out.println();
            out.println("if (option.isPresent()) {");
            out.println("return option.get();");
            out.println("}");
            out.println();
            out.println("return super.%s();".formatted(overrideMethod.getSimpleName()));
          } else {
            List<Map.Entry<Name, TypeMirror>> entries =
                parameters.stream().map(p -> Map.entry(p.getSimpleName(), p.asType())).toList();

            out.print("var option = advisor.%s(".formatted(overrideMethod.getSimpleName()));

            for (int i = 0; i < entries.size(); i++) {
              Map.Entry<Name, TypeMirror> entry = entries.get(i);
              if (i == entries.size() - 1) {
                out.print("%s".formatted(entry.getKey()));
              } else {
                out.print("%s, ".formatted(entry.getKey()));
              }
            }

            out.println(", this);");

            out.println();
            out.println("if (option.isPresent()) {");
            out.println("return option.get();");
            out.println("}");
            out.println();
            out.print("return super.%s(".formatted(overrideMethod.getSimpleName()));

            for (int i = 0; i < entries.size(); i++) {
              Map.Entry<Name, TypeMirror> entry = entries.get(i);
              if (i == entries.size() - 1) {
                out.print("%s".formatted(entry.getKey()));
              } else {
                out.print("%s, ".formatted(entry.getKey()));
              }
            }

            out.println(");");
          }
        }

        out.println("}");
        out.println();
      }
      //
      // calling override advisor methods end
      //

      out.println("}");
    }
  }
}
