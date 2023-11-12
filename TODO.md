1. remove kafka and use the vertx event bus
2. remove Main files and use the vertx launcher
   1. create dagger providers in the vertical and shutdown in the verticle
   2. use the vertx shaded builds
3. create a saga executor that can be used across restarts (persisted state)
4. play with change data capture (CDC) https://medium.com/event-driven-utopia/configuring-debezium-to-capture-postgresql-changes-with-docker-compose-224742ca5372
5. once all images are built, make sure they start correctly with docker-compose

