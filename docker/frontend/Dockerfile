FROM node:22-alpine AS build

WORKDIR /app

COPY src/moneylog/package.json package.json
COPY src/moneylog/package-lock.json package-lock.json

RUN npm ci

COPY src/moneylog/ .

ARG VITE_API_BASE_URL=http://localhost:8080/api
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

RUN npm run build

FROM nginx:1.27-alpine

COPY docker/frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
