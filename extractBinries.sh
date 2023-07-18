#!/bin/bash

rm -rf bin app.tar
mkdir bin

for img in catalog iam shop
do
  id=$(docker create "$img":native-upx-latest)
  docker cp "$id":/app - > app.tar
  docker rm -v "$id"
  tar -xf app.tar --strip-components=1
  mv app bin/"$img".upx
  rm app.tar
done
