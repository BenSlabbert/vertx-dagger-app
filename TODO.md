1. remove Main files and use the vertx launcher
   1. create dagger providers in the vertical and shutdown in the verticle
   2. use the vertx shaded builds
2. create a saga executor that can be used across restarts (persisted state)
3. play with change data capture (CDC) https://medium.com/event-driven-utopia/configuring-debezium-to-capture-postgresql-changes-with-docker-compose-224742ca5372
4. once all images are built, make sure they start correctly with docker-compose
5. revisit proguard class file minification (see: config.pro)
   1. maybe try to use Google's R8: https://r8.googlesource.com/r8#running-r8

