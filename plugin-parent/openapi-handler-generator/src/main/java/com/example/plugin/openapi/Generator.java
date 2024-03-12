/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi;

import java.io.File;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;

interface Generator {

  void generate(List<RequestResponseSchema> schemas, String basePackage, File outputDirectory)
      throws MojoExecutionException;
}
