ARG IMAGE="node:20-alpine"

FROM $IMAGE AS builder

WORKDIR /app

RUN corepack enable && corepack prepare yarn@stable --activate && yarn set version stable

RUN echo "nodeLinker: node-modules" > .yarnrc.yml

COPY package.json .
COPY yarn.lock .
RUN yarn plugin import workspace-tools && yarn workspaces focus --production

FROM $IMAGE

WORKDIR /app

COPY package.json .
COPY yarn.lock .
COPY --from=builder /app/node_modules node_modules

COPY ./build .
ADD ./entry.js .

ENTRYPOINT ["node", "entry.js"]
CMD ["node", "index.js"]
