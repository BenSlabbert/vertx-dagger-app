FROM node:18-alpine

WORKDIR /app

COPY package.json .
COPY yarn.lock .
RUN yarn --prod

COPY ./build .

CMD ["node", "index.js"]
