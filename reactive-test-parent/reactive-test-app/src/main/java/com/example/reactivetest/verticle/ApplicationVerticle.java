package com.example.reactivetest.verticle;

import com.example.reactivetest.service.PersonService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import javax.inject.Inject;
import lombok.extern.java.Log;

@Log
public class ApplicationVerticle extends AbstractVerticle {

  private final PersonService personService;

  @Inject
  public ApplicationVerticle(PersonService personService) {
    this.personService = personService;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    personService
        .create("name")
        .onSuccess(created -> log.info("created: " + created))
        .compose(created -> personService.findAll())
        .onSuccess(all -> log.info("all: " + all))
        .onSuccess(v -> startPromise.complete());
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    log.warning("stopping");
    stopPromise.complete();
  }
}
