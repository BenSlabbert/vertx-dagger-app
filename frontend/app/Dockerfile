ARG IMAGE=node:20-alpine

FROM $IMAGE AS builder

WORKDIR /app

COPY package.json .
COPY yarn.lock .
RUN yarn --prod

FROM $IMAGE

WORKDIR /app

COPY package.json .
COPY yarn.lock .
COPY --from=builder /app/node_modules node_modules

COPY ./build .

CMD ["node", "index.js"]
