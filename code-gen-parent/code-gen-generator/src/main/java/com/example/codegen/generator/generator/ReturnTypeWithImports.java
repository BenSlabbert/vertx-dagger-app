/* Licensed under Apache-2.0 2023. */
package com.example.codegen.generator.generator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

public record ReturnTypeWithImports(String printableName, Set<String> canonicalImports) {

  public static ReturnTypeWithImports of(TypeMirror typeMirror) {
    TypeKind kind = typeMirror.getKind();
    String typeAsString = typeMirror.toString();

    // generic type
    if (kind == TypeKind.TYPEVAR) {
      TypeVariable tv = (TypeVariable) typeMirror;
      TypeMirror lowerBound = tv.getLowerBound();
      TypeMirror upperBound = tv.getUpperBound();
      throw new GenerationException("generic types are not supported");
    }

    if (kind != TypeKind.DECLARED
        && kind != TypeKind.VOID
        && kind != TypeKind.ARRAY
        && !kind.isPrimitive()) {
      throw new GenerationException("TypeKind: " + kind + " is not supported");
    }

    if (kind == TypeKind.VOID) {
      return new ReturnTypeWithImports(typeAsString, Set.of());
    }

    if (kind.isPrimitive()) {
      return new ReturnTypeWithImports(typeAsString, Set.of());
    }

    if (kind == TypeKind.ARRAY) {
      return handleArray((ArrayType) typeMirror, typeAsString);
    }

    return handleDeclaredType((DeclaredType) typeMirror, typeAsString);
  }

  private static ReturnTypeWithImports handleDeclaredType(DeclaredType dt, String typeAsString) {
    List<? extends TypeMirror> typeArguments = dt.getTypeArguments();

    if (typeArguments.isEmpty()) {
      // simple case, no type params
      String substring = typeAsString.substring(typeAsString.lastIndexOf('.') + 1);
      return new ReturnTypeWithImports(substring, Set.of(typeAsString));
    }

    Set<String> imports = new HashSet<>();

    addArgumentImports(typeArguments, imports);

    int genericParamBegin = typeAsString.indexOf('<');
    int genericParamEnd = typeAsString.indexOf('>');
    String className = typeAsString.substring(0, genericParamBegin);
    imports.add(className);

    String shortClassName =
        typeAsString.substring(className.lastIndexOf('.') + 1, genericParamBegin);

    String genericParams = typeAsString.substring(genericParamBegin + 1, genericParamEnd);

    String[] split = genericParams.split(",");
    String params =
        Arrays.stream(split)
            .map(
                string -> {
                  boolean hasExtends = string.startsWith("? extends");
                  boolean hasSuper = string.startsWith("? super");

                  String substring = string.substring(string.lastIndexOf('.') + 1);

                  if (hasExtends) {
                    substring = "? extends " + substring;
                  }

                  if (hasSuper) {
                    substring = "? super " + substring;
                  }

                  return substring;
                })
            .collect(Collectors.joining(", "));

    String format = String.format("%s<%s>", shortClassName, params);

    return new ReturnTypeWithImports(format, imports);
  }

  private static void addArgumentImports(
      List<? extends TypeMirror> typeArguments, Set<String> imports) {
    for (TypeMirror tm : typeArguments) {
      if (tm.getKind() == TypeKind.DECLARED) {
        imports.add(tm.toString());
      }

      if (tm.getKind() == TypeKind.WILDCARD) {
        // generic type with wildcard
        WildcardType wt = (WildcardType) tm;
        TypeMirror extendsBound = wt.getExtendsBound();
        TypeMirror superBound = wt.getSuperBound();

        if (null != extendsBound) {
          imports.add(extendsBound.toString());
        }

        if (null != superBound) {
          imports.add(superBound.toString());
        }
      }
    }
  }

  private static ReturnTypeWithImports handleArray(ArrayType at, String typeAsString) {
    TypeMirror componentType = at.getComponentType();

    if (componentType.getKind().isPrimitive()) {
      return new ReturnTypeWithImports(typeAsString, Set.of());
    }

    String substring = typeAsString.substring(typeAsString.lastIndexOf('.') + 1);
    return new ReturnTypeWithImports(substring, Set.of(componentType.toString()));
  }
}
