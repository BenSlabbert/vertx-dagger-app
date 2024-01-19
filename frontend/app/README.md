# test

run docker image locally:

```shell
docker container run --rm -it -p 3000:3000 -e VITE_APP_HOST="http://127.0.0.1:3000" app-sveltekit:latest
```

you must specify `VITE_APP_HOST` otherwise URLs will be broken
