/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi;

import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_SOURCES;

import javax.inject.Inject;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "openapi-gen", defaultPhase = GENERATE_SOURCES, threadSafe = true)
public class OpenApiHandlerGeneratorMojo extends AbstractMojo {

  @Inject private SomeLogic someLogic;

  /** full path to the openapi file */
  @Parameter(property = "openapi-gen.path", required = true)
  private String path;

  public void execute() {
    getLog().info("path: " + path);
    getLog().info("Hello, world.");
    someLogic.implement();
  }
}
