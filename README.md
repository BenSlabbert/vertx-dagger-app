# iam app

Licensed under Apache 2.0 https://www.apache.org/licenses/LICENSE-2.0.txt

Using [dagger2](https://dagger.dev/) and [vertx](https://vertx.io/) to talk to [redis](https://redis.io/).

This repo also builds two docker images:
1. iam:jvm-latest -> JVM runtime
2. iam:native-latest -> [static graalvm image](https://www.graalvm.org/latest/reference-manual/native-image/guides/build-static-executables/#build-a-static-native-executable).

## Example config

```json
{
  "httpConfig": {
    "port": 8080
  },
  "grpcConfig": {
    "port": 50051
  },
  "redisConfig": {
    "host": "localhost",
    "port": 6379,
    "database": 0
  },
  "postgresConfig": {
    "host": "localhost",
    "username": "user",
    "password": "password",
    "port": 5432,
    "database": "db"
  },
  "verticleConfig": {
    "numberOfInstances": 1
  }
}
```

