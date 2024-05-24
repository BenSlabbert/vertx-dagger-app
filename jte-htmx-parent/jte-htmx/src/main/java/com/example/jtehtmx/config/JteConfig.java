/* Licensed under Apache-2.0 2024. */
package com.example.jtehtmx.config;

import dagger.Module;
import dagger.Provides;
import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import github.benslabbert.vertxdaggercommons.config.Config;
import java.nio.file.Path;
import javax.inject.Singleton;

@Module
public class JteConfig {

  @Provides
  @Singleton
  static TemplateEngine provideTemplateEngine(Config config) {
    return switch (config.profile()) {
      case DEV -> {
        String path =
            "/home/ben/IdeaProjects/vertx-dagger-app/jte-htmx-parent/jte-htmx/src/main/jte";
        CodeResolver codeResolver = new DirectoryCodeResolver(Path.of(path));
        yield TemplateEngine.create(codeResolver, ContentType.Html);
      }
      case PROD -> {
        TemplateEngine templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);
        templateEngine.setBinaryStaticContent(true);
        yield templateEngine;
      }
    };
  }
}
