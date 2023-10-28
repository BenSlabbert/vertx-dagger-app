1. create a saga executor that can be used across restarts (persisted state)
2. play with using virtual threads
   1. from the router, call the service using a virtual thread

   ```java
   // mockito is limiting us to java 20 for now, which means we can't use the new virtual threads until we can upgrade
   ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
   // use this for void return
   CompletableFuture<Void> fromVirtualThread =
   CompletableFuture.runAsync(() -> log.info("from virtual thread"), executorService);
   // use this for value return types
   CompletableFuture<String> stringCompletableFuture =
   CompletableFuture.supplyAsync(() -> "from virtual thread", executorService);
   io.vertx.core.Future.fromCompletionStage(stringCompletableFuture);
   ```
3. play with change data capture (CDC) https://medium.com/event-driven-utopia/configuring-debezium-to-capture-postgresql-changes-with-docker-compose-224742ca5372
4. once all images are built, make sure they start correctly with docker-compose

