1. play with using virtual threads
   1. from the router, call the service using a virtual thread
2. create payments service
   1. create a payment for user in iam
3. create a purchase_order for the user in catalog
4. create a simple saga:
   1. create a purchase_order (catalog service)
   2. create a payment (payments service)
   3. if payment fails, cancel the purchase_order
   4. if payment succeeds, mark the purchase_order as paid
5. play with change data capture (CDC) https://medium.com/event-driven-utopia/configuring-debezium-to-capture-postgresql-changes-with-docker-compose-224742ca5372

