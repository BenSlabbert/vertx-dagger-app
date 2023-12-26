/* Licensed under Apache-2.0 2023. */
package com.example.payment;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.payment.generator.entity.generated.jooq.tables.records.AccountRecord;
import com.example.payment.generator.entity.generated.jooq.tables.records.UserProjectionRecord;
import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jooq.ForeignKey;
import org.jooq.Table;
import org.jooq.TableField;
import org.junit.jupiter.api.Test;

// TODO: revisit creating this dependency graph entity creator
class TableTest {

  private static final PrintStream OUT = System.out;

  static {
    System.setProperty("org.jooq.no-tips", "true");
    System.setProperty("org.jooq.no-logo", "true");
  }

  @Test
  void testEntityCreator() {
    EntityCreator entityCreator = new EntityCreator();
    UserProjectionRecord userProjectionRecord = entityCreator.createUserProjectionRecord();
    userProjectionRecord.setId(1L);
    AccountRecord accountRecord = entityCreator.createAccountRecord(userProjectionRecord);
    assertThat(accountRecord).isNotNull();
    assertThat(accountRecord.getUserId()).isEqualTo(1L);
  }

  @Test
  void test() throws Exception {
    Map<Table<?>, List<Dependency>> tableDependencies = getTableDependencies();

    OUT.println("package com.example.payment;");
    OUT.println();

    for (Table<?> table : tableDependencies.keySet()) {
      OUT.println("import " + table.getRecordType().getCanonicalName() + ";");
    }

    OUT.println();
    OUT.println("import java.util.Objects;");
    OUT.println("import org.jooq.Field;");
    OUT.println();

    OUT.println("public class EntityCreator {");

    // get tables without dependencies first
    List<? extends Table<?>> tablesWithoutDependencies =
        tableDependencies.entrySet().stream()
            .filter(e -> e.getValue().isEmpty())
            .map(Map.Entry::getKey)
            .toList();

    for (Table<?> table : tablesWithoutDependencies) {
      String name = table.getRecordType().getSimpleName();
      OUT.println();
      OUT.println("\tpublic " + name + " create" + name + "() {");
      OUT.println("\t\treturn new " + name + "();");
      OUT.println("\t}");
    }

    List<? extends Table<?>> tablesWithDependencies =
        tableDependencies.entrySet().stream()
            .filter(e -> !e.getValue().isEmpty())
            .map(Map.Entry::getKey)
            .toList();

    for (Table<?> table : tablesWithDependencies) {
      Class<?> recordType = table.getRecordType();
      String name = recordType.getSimpleName();
      List<Dependency> dependencies = tableDependencies.get(table);

      for (var dependency : dependencies) {
        if (dependency.referenceJavaType != Long.class) {
          throw new IllegalArgumentException("expecting Long");
        }
      }

      OUT.println();
      OUT.print("\tpublic " + name + " create" + name + "(");

      List<String> methodArgs =
          dependencies.stream()
              .map(
                  dependency -> {
                    String refRecordName =
                        dependency.referenceTable.getRecordType().getSimpleName();
                    return refRecordName + " " + toVariableName(refRecordName);
                  })
              .toList();

      OUT.print(String.join(", ", methodArgs));
      OUT.println(") {");

      String variable = toVariableName(name);
      OUT.println("\t\t" + name + " " + variable + " = new " + name + "();");

      OUT.println("\t\tLong temp = 0L;");
      OUT.println("\t\tField<Long> field = null;");
      OUT.println();

      for (var dependency : dependencies) {
        String dependencyVariableName =
            toVariableName(dependency.referenceTable.getRecordType().getSimpleName());

        OUT.println(
            "\t\ttemp = "
                + dependencyVariableName
                + ".get(\""
                + dependency.referenceColumn
                + "\", Long.class);");

        OUT.println("\t\tObjects.requireNonNull(temp);");

        OUT.println(
            "\t\tfield = fieldWithDependency("
                + variable
                + ", \""
                + dependency.fieldWithReference.getName()
                + "\");");

        OUT.println("\t\t" + variable + ".set(field, temp);");
        OUT.println();
      }

      OUT.println("\t\treturn " + variable + ";");
      OUT.println("\t}");
    }

    OUT.println();
    OUT.println(
        "\tprivate Field<Long> fieldWithDependency(org.jooq.Record recordInstance, String"
            + " fieldName) {");
    OUT.println("\t\treturn (Field<Long>)");
    OUT.println("\t\t\trecordInstance");
    OUT.println("\t\t\t\t.fieldStream()");
    OUT.println("\t\t\t\t.filter(f -> f.getName().equals(fieldName))");
    OUT.println("\t\t\t\t.filter(f -> f.getType() == Long.class)");
    OUT.println("\t\t\t\t.findFirst()");
    OUT.println("\t\t\t\t.orElseThrow();");
    OUT.println("\t}");

    OUT.println("}");
    OUT.println();
  }

  @NotNull private Map<Table<?>, List<Dependency>> getTableDependencies() throws IOException {
    Set<? extends Table<?>> tables =
        ClassPath.from(ClassLoader.getSystemClassLoader()).getAllClasses().stream()
            .filter(
                clazz ->
                    clazz
                        .getPackageName()
                        .equalsIgnoreCase(
                            "com.example.payment.generator.entity.generated.jooq.tables"))
            .map(ClassPath.ClassInfo::load)
            .filter(Table.class::isAssignableFrom)
            // remove inner classes
            .filter(f -> !f.isMemberClass())
            .map(
                c -> {
                  try {
                    // jooq tables have empty constructors
                    return (Table<?>) c.getDeclaredConstructor().newInstance();
                  } catch (Exception e) {
                    throw new RuntimeException(e);
                  }
                })
            .collect(Collectors.toSet());

    Map<Table<?>, List<Dependency>> tableDependencies = new HashMap<>();
    tables.forEach(t -> tableDependencies.put(t, new ArrayList<>()));

    for (Table<?> table : tables) {
      for (ForeignKey<?, ?> reference : table.getReferences()) {

        TableField<?, ?> fieldWithReference = getSingleElement(reference.getFields());
        Class<?> type = fieldWithReference.getType();
        if (type != Long.class) {
          throw new IllegalArgumentException("expecting Long");
        }
        TableField<?, ?> referenceField = getSingleElement(reference.getKeyFields());

        Table<?> referenceTable = referenceField.getTable();
        String referenceColumn = referenceField.getName();
        Class<?> referenceJavaType = referenceField.getType();

        tableDependencies.computeIfPresent(
            table,
            (k, v) -> {
              v.add(
                  new Dependency(
                      (TableField<?, Long>) fieldWithReference,
                      referenceTable,
                      referenceColumn,
                      referenceJavaType));
              return v;
            });
      }
    }
    return tableDependencies;
  }

  record Dependency(
      TableField<?, Long> fieldWithReference,
      Table<?> referenceTable,
      String referenceColumn,
      Class<?> referenceJavaType) {}

  private <T> T getSingleElement(Iterable<T> iterable) {
    Iterator<T> itr = iterable.iterator();

    boolean hasNext = itr.hasNext();
    if (!hasNext) {
      throw new NoSuchElementException("iterable is empty");
    }

    T next = itr.next();

    hasNext = itr.hasNext();
    if (hasNext) {
      throw new IndexOutOfBoundsException("iterable has more than 1 element");
    }

    return next;
  }

  private String toVariableName(String val) {
    String lowerCase = val.substring(0, 1).toLowerCase();
    return lowerCase + val.substring(1);
  }
}
