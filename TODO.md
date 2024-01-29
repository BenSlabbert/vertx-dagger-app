# Tasks

1. client-parent/delivery-truck-client
   1. this currently can authenticate with iam
   2. we need to add a warehouse service that will take successful orders and manage deliveries
   3. then we can create a number of these delivery trucks to deliver the orders

# Todos

1. create a saga executor that can be used across restarts (persisted state)
2. play with change data capture (CDC) https://medium.com/event-driven-utopia/configuring-debezium-to-capture-postgresql-changes-with-docker-compose-224742ca5372
3. once all images are built, make sure they start correctly with docker-compose
4. revisit proguard class file minification (see: config.pro)
   1. maybe try to use Google's R8: https://r8.googlesource.com/r8#running-r8

