/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi;

import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_SOURCES;

import java.io.File;
import java.util.List;
import javax.inject.Inject;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "openapi-gen", defaultPhase = GENERATE_SOURCES, threadSafe = true)
public class OpenApiHandlerGeneratorMojo extends AbstractMojo {

  @Inject private SchemaParser schemaParser;
  @Inject private Generator generator;

  /** full path to the openapi file */
  @Parameter(property = "openapi-gen.file", required = true)
  private File file;

  /** base java package for generated classes */
  @Parameter(property = "openapi-gen.basePackage", required = true)
  private String basePackage;

  /** output directory for generated sources */
  @Parameter(property = "openapi-gen.outputDirectory", required = true)
  private File outputDirectory;

  public void execute() throws MojoExecutionException {
    getLog().info("file: " + file);
    getLog().info("basePackage: " + basePackage);
    getLog().info("outputDirectory: " + outputDirectory);

    // todo ensure that if the path exists it is a directory
    //  this evaluates to false if it does not exist
    //    if (!outputDirectory.isDirectory()) {
    //      throw new MojoExecutionException("outputDirectory must be a directory");
    //    }

    List<RequestResponseSchema> schemas = schemaParser.parseFile(file);
    generator.generate(schemas, basePackage, outputDirectory);
  }
}
